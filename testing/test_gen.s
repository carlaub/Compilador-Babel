	.data
	ecert: .asciiz "cert"
	efals: .asciiz "fals"
	ejump: .asciiz "\n"
	err_out_of_bounds: .asciiz "Accés invàlid al vector"
	.text
main:
	li	$t0,	10
	sw	$t0,	-8($gp)
	lw	$t0,	-8($gp)
	#Negacio
	neg	$t0,	$t0
	add	$t0,	$t0,	2
	sw	$t0,	-16($gp)
	lw	$t0,	-16($gp)
	#Escriure
	li	$v0,	1
	move	$a0,	$t0
	syscall
	li	$v0,	11
	la	$a0,	ejump
	syscall
	li	$t1,	0x1
	sw	$t1,	-12($gp)
	lw	$t1,	-12($gp)
	#NOT
	not	$t1,	$t1
	andi	$t1,	$t1,	 0x00000001
	sw	$t1,	-20($gp)
	lw	$t1,	-20($gp)
	#Escriure
	beqz	$t1,	eti0
	li	$v0,	4
	la	$a0,	ecert
	b	eti1
	eti0:
	li	$v0,	4
	la	$a0,	efals
	eti1:
	syscall
	li	$v0,	11
	la	$a0,	ejump
	syscall
	#Escriure
	.data
	eti2: .asciiz "hola"
	.text
	li	$v0,	4
	la	$a0,	eti2
	syscall
	lw	$s0,	-16($gp)
	#Escriure
	li	$v0,	1
	move	$a0,	$s0
	syscall
	li	$v0,	11
	la	$a0,	ejump
	syscall
	jr $ra
