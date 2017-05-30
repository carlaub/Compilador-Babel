	
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
	sw	$t1,	-28($gp)
	#Escriure
	
.data
	_eti0: .asciiz "hola "
	
.text
	li	$v0,	4
	la	$a0,	_eti0
	syscall
	lw	$t1,	-28($gp)
	#Escriure
	li	$v0,	1
	move	$a0,	$t1
	syscall
	li	$v0,	11
	la	$a0,	ejump
	syscall
	li	$s0,	0x0
	sw	$s0,	-20($gp)
	li	$s0,	0x0
	sw	$s0,	-24($gp)
	#Init funció
	addi	$sp,	$sp,	-72
	sw	$fp,	0($sp)
	addi	$sp,	$sp,	-12
	lw	$s0,	-20($gp)
	sw	null,	-20($gp)
	lw	$t2,	-8($gp)
	#OP_REL ==
	seq	$t2,	$t2,	3
	sw	$t2,	-32($gp)
	#Escriure
	
.data
	_eti1: .asciiz "result logic: "
	
.text
	li	$v0,	4
	la	$a0,	_eti1
	syscall
	lw	$t2,	-32($gp)
	#Escriure
	beqz	$t2,	_eti2
	li	$v0,	4
	la	$a0,	ecert
	b	_eti3
	
_eti2:
	li	$v0,	4
	la	$a0,	efals
	
_eti3:
	syscall
	li	$v0,	11
	la	$a0,	ejump
	syscall
	#read
	li	$v0,	5
	syscall
	move	$s1,	$v0
	sw	$s1,	-16($sp)
	#Escriure
	
.data
	_eti4: .asciiz "Esto es hola = "
	
.text
	li	$v0,	4
	la	$a0,	_eti4
	syscall
	lw	$t3,	-16($gp)
	#Escriure
	li	$v0,	1
	move	$a0,	$t3
	syscall
	li	$v0,	11
	la	$a0,	ejump
	syscall
	jr	$ra
