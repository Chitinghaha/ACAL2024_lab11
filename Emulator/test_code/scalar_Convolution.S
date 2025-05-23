.data
## input data size = 2x8x8
input_data:
.byte 0 1 2 3 4 5 6 7
.byte 0 1 2 3 4 5 6 7
.byte 0 1 2 3 4 5 6 7
.byte 0 1 2 3 4 5 6 7
.byte 0 1 2 3 4 5 6 7
.byte 0 1 2 3 4 5 6 7
.byte 0 1 2 3 4 5 6 7
.byte 0 1 2 3 4 5 6 7

.byte 0 1 2 3 4 5 6 7
.byte 0 1 2 3 4 5 6 7
.byte 0 1 2 3 4 5 6 7
.byte 0 1 2 3 4 5 6 7
.byte 0 1 2 3 4 5 6 7
.byte 0 1 2 3 4 5 6 7
.byte 0 1 2 3 4 5 6 7
.byte 0 1 2 3 4 5 6 7

## kernel size 2x3x3
kernel_data:
.byte 0 1 2
.byte 0 1 2
.byte 0 1 2

.byte 0 1 2
.byte 0 1 2
.byte 0 1 2

## output data size 1x5x5
output_data:
.byte 0 0 0 0 0 0
.byte 0 0 0 0 0 0
.byte 0 0 0 0 0 0
.byte 0 0 0 0 0 0
.byte 0 0 0 0 0 0
.byte 0 0 0 0 0 0


.text
main:

li sp,0x4
li a2,8
li a3,3
li a4,6
la a5,input_data
la a6,kernel_data
la a7,output_data

addi sp,sp,-4
sw ra,0(sp)
jal ra, Conv2D
addi a5,a5,64
addi a6,a6,9
jal ra,Conv2D
lw ra,0(sp)
addi sp,sp,4
lw s6,0(a7)
lw s7,6(a7)
lw s8,12(a7)
lw s9,18(a7)
lw s10,24(a7)
lw s11,30(a7)

hcf

Conv2D:
srli s0,a3,1
li t0,0
bge t0,a4,endLoop1
Loop1:
li t1,0
bge t1,a4,endLoop2
Loop2:
li t2,0
bge t2,a3,endLoop3
Loop3:
li t3,0
bge t3,a3,endLoop4
Loop4:
sub s1,t2,s0
add s1,s1,t0
addi s1,s1,1
sub s2,t3,s0
add s2,s2,t1
addi s2,s2,1

mul s3,s1,a2
add s3,s3,s2
add s3,s3,a5
lb s3,0(s3)

mul s4,t2,a3
add s4,s4,t3
add s4,s4,a6
lb s4,0(s4)

mul s3,s3,s4

mul s4,t0,a4
add s4,s4,t1
add s4,s4,a7
lb s5,0(s4)

add s5,s5,s3
sb s5,0(s4)

addi t3,t3,1
blt t3,a3,Loop4
endLoop4:
addi t2,t2,1
blt t2,a3,Loop3
endLoop3:
addi t1,t1,1
blt t1,a4,Loop2
endLoop2:
addi t0,t0,1
blt t0,a4,Loop1
endLoop1:
    ret
