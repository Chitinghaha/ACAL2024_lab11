package SoC

import chisel3._
import chisel3.util._

// CPU <-> Bus <-> Memory

class top_SoC(idWidth: Int, addrWidth: Int, dataWidth: Int) extends Module {
    val io = IO(new Bundle{
        val regs = Output(Vec(32,UInt(dataWidth.W)))
        val Hcf = Output(Bool())

        //for sure that IM and DM will be synthesized
        val inst = Output(UInt(32.W))
        val rdata = Output(UInt(32.W))
        val wdata  = Output(UInt(32.W))

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
        val raddr = Output(UInt(32.W))
        val WB_rd = Output(UInt(5.W))
        val WB_wdata = Output(UInt(32.W))
        val EXE_Jump = Output(Bool())
        val EXE_Branch = Output(Bool())
    })
    // Initialization
    io.Hcf := true.B
    io.inst := 0.U
    io.rdata := 0.U
    io.wdata := 0.U
    io.E_Branch_taken := false.B
    io.Flush := false.B
    io.Stall_MA := false.B
    io.Stall_DH := false.B
    io.IF_PC := 0.U
    io.ID_PC := 0.U
    io.EXE_PC := 0.U
    io.MEM_PC := 0.U
    io.WB_PC := 0.U
    io.EXE_alu_out := 0.U
    io.EXE_src1 := 0.U
    io.EXE_src2 := 0.U
    io.ALU_src1 := 0.U
    io.ALU_src2 := 0.U
    io.raddr := 0.U
    io.WB_rd := 0.U
    io.WB_wdata := 0.U
    io.EXE_Jump := false.B
    io.EXE_Branch := false.B

    // Initialize the registers with 0
    for (i <- 0 until 32) {
        io.regs(i) := 0.U
    }
}