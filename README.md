NYCU_313591016_丁驥 ACAL 2024 Spring Lab 11 HW 
===


###### tags: `Lab 11` `Homework`

[TOC]

## 作業說明

You can check out different branches to represent various assignments or organize specific assignments using folders. You can decide on the branch names yourselves.

## Homework 11-1 SoC Integration with CPU

> Please paste the link close to the parentheses
- [Hw 11-1 Branch link](https://course.playlab.tw/git/Jim0628/2025_lab11)

1. Revise system configuration and design address mapping scheme(Explain how you design)
   設定 Master 數量為 1 (CPU)，Slave 數量為 1 (Memory):

- 系統中僅有一個 Master，即 CPU，和一個 Slave，即 Memory。
- 設定 Memory Base Address 為 0x8000，Height 為 256，Width 為 32，Size 為 Height * Width = 8192:

    - Memory 的基本地址設為 0x8000。
    - Memory 的高度設為 256。
    - Memory 的寬度設為 32。
    - Memory 的大小為 Height * Width，即 8192。
- 其他設定 (idAddress, addrWidth, dataWidth) 設為 (4, 32, 32):

    - idAddress 設為 4。
    - addrWidth 設為 32。
    - dataWidth 設為 32。


2. Integrate CPU, bus and memory 
 我採取跟DMA一樣的方式，直接利用AXI在CPU做好連接，用以下列方式可以較直觀的串接。
 ```scala=
    bus.io.masters(0) <> cpu.io.DataMem
    bus.io.slaves(0) <> mem.io.slave
 ```

3. Run the **Emulator/test_code/scalar_Convolution_2D.S** and paste the result(Screenshot)
- 從8092開始看，數字是對的
![](https://course.playlab.tw/md/uploads/4b8b6054-09a2-4499-b963-73e40a97fe94.png)






## Homework 11-2 Performance Enhancement Using DMA

> Please paste the link close to the parentheses
- [Hw 11-2 Branch link](https://course.playlab.tw/git/Jim0628/2025_lab11/-/tree/HW11-2?ref_type=heads)

1. Revise system configuration and design address mapping scheme(Explain how you design)
    - System configuration:
        - master 數量改為 2，slave 改為 3 
        - memory
            - Local memory
                - 跟第一題的MEM一樣(DataMemBaseAddr: Int = 0x10000)
            - Global memory 
                - 跟第一題的MEM一樣(GlobalMemBaseAddr: Int = 0x8000)
        - DMA base address為0x0

       

2. Integrate CPU, bus, memory and dma 
    :::warning
    Please remember that you need to set up both local memory and global memory, and configure different latencies. During initialization, data will be saved in global memory. The CPU needs to program the DMA to transfer data from local memory to global memory. When data movement is complete, the CPU will access data in local memory.
    :::
也是採取跟第一題一樣的方式，dma 已經在LAB 有提過了，所以直接接上即可，程式碼大致如下：
```scala=
// bus connection
bus.io.masters(0) <> cpu.io.DataMem
bus.io.slaves(0) <> localMem.io.slave
bus.io.masters(1) <> dma.io.master
bus.io.slaves(1) <> globalMem.io.slave
bus.io.slaves(2) <> dma.io.slave
 ```
    
3. Revise software program and generate binary files using emulator(Explain how you revise the program)

    我在 Emulator 的 test_code 新增一個 HW11-2.S，用來完成題目的要求，以下我會講解流程
 
    - 準備資料與設定DMA
        - 設定 SOURCE_INFO(Global memory)。
        - 設定 DEST_INFO(Local memory)。
        - 設定 DMA_SIZE_CFG 為 0x040404ff，設定資料大小。
            - W = 4 ， H=ff
            - strde = 4 (全搬的意思)
        - enable DMA 並等待其完成(sw t1, 0(t0))。
    - 準備進行 Conv2D 運算
        - 調整 input_data、kernel_data 和 output_data 的address
    - 執行 Conv2D 運算
        - 呼叫 Conv2D 函數。
        - 將計算後的結果存回 output_data。
    - DMA 完成後的資料移動
        - DMA 運行完成後，將 local memory 的結果移回 global memory 的對應位置。
    - 最終操作與結束
        - 完成所有操作後，插入 nop。
   
4. Run the **Emulator/test_code/scalar_Convolution_2D.S** and paste the result(Screenshot)

![](https://course.playlab.tw/md/uploads/5c915a21-9cea-41db-9c4f-a094822b413c.png)


說明：可以看到DMA 有成功的在local mem 和 global mem之間搬資料。


## Homework 11-3 Support and AXI Bus Implemention with Burst Mode

> Please paste the link close to the parentheses
- [Hw 11-3 Branch link](https://course.playlab.tw/git/Jim0628/2025_lab11/-/tree/HW11-3?ref_type=heads)
 

1. Upgrade your AXI bus design
    (1). Explain your design and how you test your AXI bus.
    (2). Provide your testbench command. Paste the result here (Screenshot).

    第1小題
    - 先在testUtils.scala 增加 genBurstAXIAddr function，用來做test。
    ```scala=
    def genAXIAddrBurst(addr: BigInt , len: Int ): Axi4Request = { 
        val req = new Axi4Request(AXI_Config.s_id_width, AXI_Config.addr_width, AXI_Config.data_width).Lit(
            _.addr -> addr.U,
            _.burst -> 1.U, 
            _.cache -> 0.U,
            _.id    -> 0.U,
            _.len   -> len.U, 
            _.lock  -> 0.U,
            _.prot  -> 0.U,
            _.qos   -> 0.U,
            _.region -> 0.U,
            _.size  -> 2.U
        )
        req
    }

    ```
    - 我利用AXI_lite的格式來做測試，但我把當中的 genAXIAddr(addr: BigInt) 改成genBurstAXIAddr(addr: BigInt , len: Int )，讓函數中的參數有 len ，用以支援Burst，例如以下範例。
    ![](https://course.playlab.tw/md/uploads/d48b8538-4c9f-43b4-ac86-dfd8551657b5.png)
修改成
![](https://course.playlab.tw/md/uploads/de5d1177-36e4-4404-a06e-6db0e23a1105.png)

    - 修改bus 內部的 scala，加上 register 等 Burst 需要的功能。
    ```scala=
      // add for burst
      val read_burst_reg = RegInit(0.U(2.W))          
      val read_burst_reg_len = RegInit(0.U(8.W))   
    ```
    - 做test，結果分別如第2小題所示。

第2小題
- AXIReadBusBurstTest
![](https://course.playlab.tw/md/uploads/41f406be-97b4-40ba-ba70-1aa07919c4d9.png)


- AXIWriteBusBurstTest
![](https://course.playlab.tw/md/uploads/3169c18a-7856-4347-b0d8-762478e19e9b.png)

- AXISlaveReadMuxBurstTest
![](https://course.playlab.tw/md/uploads/8d615b36-73b0-4e9f-80e2-487946a146f8.png)

- AXISlaveWriteMuxBurstTest
![](https://course.playlab.tw/md/uploads/55afa0df-8898-439f-b91d-c3194ca490ac.png)

- AXIXBarTest
![](https://course.playlab.tw/md/uploads/ae9fbc17-fb0a-4b33-b391-43cfd3473c12.png)









2. Modify **Data memory**, and **DMA controller (Interface)** to support **AXI Burst Mode**.
    (1) Modify `AXI_IF.scala`. Use FSM to record and count the transaction address in the burst mode.
    (2) Modify`Hardware/Lab/src/main/scala/acal_lab11/Memory/DataMem_AXI.scala`. Use FSM to record and count the transaction address in the burst mode.

    A : 這邊主要是修改 DataMem.scala，因為我沒有另外寫AXI_IF，所以counter或 state 的內容修改都寫在 DataMem.scala，詳細實作可見該檔案，另外也要改DMA的 burst mode ，把它改成1(雖然後面沒用到)。

3. Do performance analysis to explain why the AXI Burst mode helps the performance.
 
    :::warning
    Run the **Emulator/test_code/scalar_Convolution_2D.S** test on your CPU and 
    paste the result Here (Screenshot).
    :::
    
    
    - HW1 (Without burst mode): ![](https://course.playlab.tw/md/uploads/866716e6-fafa-481d-8585-a92ca4474d0f.png)


    - HW3 (With burst mode): ![](https://course.playlab.tw/md/uploads/969178a0-6992-4dd5-ad08-503e916a6062.png)





    使用 AXI 的 Burst mode 可以減少完成相同數據傳輸所需的 total cycles，這是因為數據可以連續傳輸，中間不需要頻繁的 handshaking ，可以從圖中看到，cycle 數量從119751 下降到 108069，這意味著系統在相同時間內可以完成更多的工作，提高 Data 傳輸量和效率。

## Homework 11-4 Performance Analysis and Comparison

> This section requires you to answer the questions in the document. You can directly analyze the results or experiment with different hardware configurations or software programs to analyze the results and then write down your thoughts.

可以看到在我們修改 Burst mode 後，在cycle數量方面，如同上面所說的從119751 下降到 108069，提升了 9.7% ，在時間方面，Latency 設為 20 的情況下，從 209 降到了 165，提升了 21.05%，可以看到隨著 cycle 數量減少，實際的傳輸時間也有所改善，因此 Burst 在以上的比較中是有效提升速度的。




