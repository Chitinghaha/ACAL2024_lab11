package SoC

import chisel3._
import chisel3.util._

import PiplinedCPU._
import Memory._
import MemIF._
import AXI._
import DMA._
import AXILite._

// CPU <-> Bus <-> Memory

class top_SoC(idWidth: Int, addrWidth: Int, dataWidth: Int) extends Module {
    val io = IO(new Bundle{
        val regs = Output(Vec(32,UInt(dataWidth.W)))
        val Hcf = Output(Bool())
        val inst = Output(UInt(32.W))
        val Dump_Mem = Input(Bool())
        // Test
        val E_Branch_taken = Output(Bool())
        val Flush = Output(Bool())
        val Stall_MA = Output(Bool())
        val Stall_DH = Output(Bool())
        val IF_PC = Output(UInt(32.W))
        val ID_PC = Output(UInt(32.W))
        val EXE_PC = Output(UInt(32.W))
        val MEM_PC = Output(UInt(32.W))
        val WB_PC = Output(UInt(32.W))
        val EXE_alu_out = Output(UInt(32.W))
        val EXE_src1 = Output(UInt(32.W))
        val EXE_src2 = Output(UInt(32.W))
        val ALU_src1 = Output(UInt(32.W))
        val ALU_src2 = Output(UInt(32.W))
        val WB_rd = Output(UInt(5.W))
        val WB_wdata = Output(UInt(32.W))
        // add 
        val rdata = Output(UInt(32.W))
        val raddr = Output(UInt(32.W))

        val wdata  = Output(UInt(32.W))
        val waddr = Output(UInt(32.W))
        val EXE_Jump = Output(Bool())
        val EXE_Branch = Output(Bool())
    })

    object SystemConfig {
        val nMasters: Int = 1
        val nSlaves: Int = 1
        val DMABaseAddr: Int = 0
        val DMASize: Int = 100
        val DataMemBaseAddr: Int = 0x8000 //跟範例設定一樣
        val DataMemSize: Map[String, Int] = Map(
        "Size" -> 8192, // 256*32
        "Height" -> 256,
        "Width" -> 32 // 32 bits
        )
        val DataMemLatency: Int = 20
        val DataMemInitFilePath: String =
        "./src/main/resource/data_HW.hex" // Provide the file path
    }
// ================== datamem ======================
    val mem = Module(
        new DataMem(
        SystemConfig.DataMemSize("Width"),
        SystemConfig.DataMemSize("Height"),
        idWidth,
        addrWidth,
        dataWidth,
        SystemConfig.DataMemBaseAddr,
        SystemConfig.DataMemLatency,
        SystemConfig.DataMemInitFilePath
        )
    )

// ================== bus ======================
    val bus = Module(
        new AXILiteXBar(
        SystemConfig.nMasters,
        SystemConfig.nSlaves,
        idWidth,
        addrWidth,
        dataWidth,
        Seq(
            (SystemConfig.DataMemBaseAddr, SystemConfig.DataMemSize("Size"))
        )
        )
    )

// ================== instMem ======================
    val im = Module(new InstMem(15))
    mem.io.dump := io.Dump_Mem


// ================== cpu ======================
    val cpu = Module(new PiplinedCPU(addrWidth, dataWidth))
    cpu.io.InstMem.rdata := im.io.inst
    cpu.io.InstMem.Valid := true.B

    im.io.raddr := cpu.io.InstMem.raddr


    bus.io.masters(0) <> cpu.io.DataMem
    bus.io.slaves(0) <> mem.io.slave

    // Test
    io.regs := cpu.io.regs
    io.E_Branch_taken := cpu.io.E_Branch_taken
    io.Flush := cpu.io.Flush
    io.Stall_MA := cpu.io.Stall_MA
    io.Stall_DH := cpu.io.Stall_DH
    io.Hcf := cpu.io.Hcf
    io.IF_PC := cpu.io.IF_PC
    io.ID_PC := cpu.io.ID_PC
    io.EXE_PC := cpu.io.EXE_PC
    io.MEM_PC := cpu.io.MEM_PC
    io.WB_PC := cpu.io.WB_PC
    io.EXE_alu_out := cpu.io.EXE_alu_out
    io.EXE_src1 := cpu.io.EXE_src1
    io.EXE_src2 := cpu.io.EXE_src2
    io.ALU_src1 := cpu.io.ALU_src1
    io.ALU_src2 := cpu.io.ALU_src2
    io.WB_rd := cpu.io.WB_rd
    io.WB_wdata := cpu.io.WB_wdata
    io.EXE_Jump := cpu.io.EXE_Jump
    io.EXE_Branch := cpu.io.EXE_Branch
    io.inst := im.io.inst
    io.rdata := mem.io.slave.r.bits.data
    io.wdata := mem.io.slave.w.bits.data
    io.raddr := cpu.io.raddr
    io.waddr := cpu.io.waddr
}