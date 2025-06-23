Have to make copies in every convert function of transparency type bufferedimage.

Rain tiles:
    Color in hexadecimalPrecipitation intensity in mm/h
        #FFFFFFNo data for this area
        #000000     < 0.03, in practice no precipitation
        #91E4FF     < 0.05
        #5ED7FF     < 0.2
        #00AAFF     < 1
        #0080FF     < 5
        #0055FF     < 15
        #7A8715     < 

Cloud tiles:
    Cloudcover percentage is on the red component of each pixel. red/2 = cloud% Therefor red values from 0 to 200

Temp tiles:
    Temp given on the red channel. Temp in c is red-128
        #780030
        #B21002
        #FF5D56
        #FFB37A
        #FFF36E
        #D0F5D9
        #85E4ED
        #6AC6EE
        #649BE2
        #114F9D
        #481581

Wind tiles:
    Wind X component is given on red channel. Wind Y component is given on green channel. (wind speed*2)+128
        #310047     >32.6 
        #4d0a6c     28.5 
        #5B278D     24.5 
        #7043A8     20.8 
        #7B57ED     17.2 
        #4B87EA     13.9 
        #13A8D6     10.8 
        #3CBEBE     8 
        #79CCAC     5.5 
        #A7CEA1     <5.4 