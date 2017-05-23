	.data
	ecert: .asciiz "cert"
	efals: .asciiz "fals"
	ejump: .asciiz "\n"
	err_out_of_bounds: .asciiz "Accés invàlid al vector"
	.text
main:
	li	$t0,	4
	sw	$t0,	-8($gp)
	li	$t0,	3
	sw	$t0,	-12($gp)
	lw	$t0,	-8($gp)
	lw	$t1,	-12($gp)
	add	$t0,	$t0,	$t1
	sw	$t0,	-24($gp)
	#Escriure
	.data
	eti0: .asciiz "hola "
	.text
	li	$v0,	4
	la	$a0,	eti0
	syscall
	lw	$t0,	-24($gp)
	#Escriure
	li	$v0,	1
	move	$a0,	$t0
	syscall
	li	$v0,	11
	la	$a0,	ejump
	syscall
	li	$t1,	0x0
	sw	$t1,	-16($gp)
	li	$t1,	0x0
	sw	$t1,	-20($gp)
	lw	$t1,	-8($gp)
	sgt	$t1,	$t1,	3
	sw	$t1,	-28($gp)
	#Escriure
	.data
	eti1: .asciiz "result logic: "
	.text
	li	$v0,	4
	la	$a0,	eti1
	syscall
	lw	$t1,	-28($gp)
	#Escriure
	beqz	$t1,	eti2
	li	$v0,	4
	la	$a0,	ecert
	b	eti3
	eti2:
	li	$v0,	4
	la	$a0,	efals
	eti3:
	syscall
	li	$v0,	11
	la	$a0,	ejump
	syscall
	jr $ra
