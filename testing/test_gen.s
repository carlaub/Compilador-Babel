	.data
	ecert: .asciiz "cert"
	efals: .asciiz "fals"
	ejump: .asciiz "\n"
	err_out_of_bounds: .asciiz "Accés invàlid al vector"
	.text
main:
	li	$t0,	8
	sw	$t0,	-8($gp)
	lw	$t0,	-8($gp)
	add	$t0,	$t0,	4
	mul	$t0,	$t0,	4
	lw	$t1,	-8($gp)
	sub	$t1,	$t1,	5
	add	$t0,	$t0,	$t1
	add	$t0,	$t0,	2
	sw	$t0,	-12($gp)
	lw	$t0,	-12($gp)
	#Escriure
	li	$v0,	1
	move	$a0,	$t0
	syscall
	li	$v0,	11
	la	$a0,	ejump
	syscall
	lw	$t1,	-0($gp)
	sw	$t1,	-0($gp)
	jr $ra
