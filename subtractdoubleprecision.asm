ORG 0
CLE     /Clear E
LDA BL  /Load lower B
CMA     /Compliment AC
INC     /Increment AC (now holds 2s compliment of BL)
ADD AL  /Does subtraction of AL - BL
STA CL  /Stores result in CL
CLA     /Clear AC
CIL     /Circular shift left to bring E to LSB
STA TMP /Store E in TMP
LDA BH  /Load higher B
CMA     /Complment AC
INC     /Increment AC (now holds 2s compliment of BH)
ADD AH  /Does subtraction of AH - BH
ADD TMP /Adds carry to result
STA CH  /Stores result in CH
HLT
TMP, HEX 0
AL, HEX FFFF  /A Low  (First FFFF)
AH, HEX FF32  /A High (Second FFFF)
BL, HEX 015F  /B Low  (First FFFF) 
BH, HEX FF53  /B High (Second FFFF)
CL, HEX 0     /Result of A - B
CH, HEX 0
END