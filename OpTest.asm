reset:
    ldi r10, 4095
    out 4, r10

main:
    input_loop:
        in r2, 3
        tst r2
        breq input_loop

    out 3, r2
    rjmp input_loop
hlt
