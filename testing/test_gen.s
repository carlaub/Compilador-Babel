	
.data
	ecert: .asciiz "cert"
	efals: .asciiz "fals"
	ejump: .asciiz "\n"
	err_out_of_bounds: .asciiz "Accés invàlid al vector"

.text
main:
	move	$fp,	$sp
	lw	$t0,	-0($gp)
	#NOT
	not	$t0,	$t0
	andi	$t0,	$t0,	 0x00000001
	li	$t1,	4
	sw	$t1,	-8($gp)
	li	$t1,	3
	sw	$t1,	-12($gp)
	lw	$t1,	-8($gp)
	lw	$s0,	-12($gp)
	add	$t1,	$t1,	$s0
	sw	$t1,	-24($gp)
	#Escriure
	
.data
	eti-0: .asciiz "hola "
	
.text
	li	$v0,	4
	la	$a0,	eti-0
	syscall
	lw	$t1,	-24($gp)
	#Escriure
	li	$v0,	1
	move	$a0,	$t1
	syscall
	li	$v0,	11
	la	$a0,	ejump
	syscall
	li	$s0,	0x0
	sw	$s0,	-16($gp)
	li	$s0,	0x0
	sw	$s0,	-20($gp)
	#Init funció
	addi	$sp,	$sp,	-72
	sw	$fp,	0($sp)
	addi	$sp,	$sp,	-12
	lw	$s0,	-16($gp)
	sw	null,	-16($gp)
	lw	$t2,	-8($gp)
	#OP_REL ==
	seq	$t2,	$t2,	3
	sw	$t2,	-28($gp)
	#Escriure
	
.data
	eti-1: .asciiz "result logic: "
	
.text
	li	$v0,	4
	la	$a0,	eti-1
	syscall
	lw	$t2,	-28($gp)
	#Escriure
	beqz	$t2,	eti-2
	li	$v0,	4
	la	$a0,	ecert
	b	eti-3
	
eti-2:
	li	$v0,	4
	la	$a0,	efals
	
eti-3:
	syscall
	li	$v0,	11
	la	$a0,	ejump
	syscall
	jr	$ra
