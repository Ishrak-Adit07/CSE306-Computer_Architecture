#include <avr/io.h>
#define F_CPU 1000000
#include <util/delay.h>

unsigned char regArr[16];
//0 -> zero
//1 -> t0
//2 -> t1
//3 -> t2
//4 -> t3
//5 -> t4
//6 -> sp



int main(void)
{
	MCUCSR|=(1<<JTD);
	MCUCSR|=(1<<JTD);
	
	
	DDRA=0XFF;    //Read data1 & Read data2
	DDRB=0x00;    //Instruction inputs
	DDRC=0x00;    //Clock, flags, write data
	DDRD=0x80;    //Source reg 1 and Show register selections
	
	for(int i=1; i<16; i++){
		regArr[i]=i;
	}
	regArr[0]=0;
	regArr[6]=15;

	int currclk=0;
	int regwrite;
	int showreg;
	int reset;
	int prevclk=0;
	
	while (1)
	{
		int b = PINB;
		int muxout = (b%16) & 0x0F; //Instruction: 0-3 / 4-7
		int reg2 = b>>4; //Instruction: 4-7 /
		int d = PIND;
		int reg1 = d%16; //Source register1 / Instruction: 8-11
		unsigned char controlFlags = PINC;
		//Clock
		prevclk = currclk;
		currclk = (controlFlags & 0b0001);
		//Flags
		regwrite = (controlFlags & 0b0010)>>1;
		showreg = (controlFlags & 0b0100)>>2; //Which register to show
		reset = (controlFlags & 0b1000)>>3;
		unsigned char writeData = controlFlags>>4;
		
		PORTD = PORTD | (1<<7);

		if(showreg==1){
			int showRegSelections = ( (PIND>>4) & (0b0111) ) ;
			PORTA = regArr[showRegSelections];
		}
		else{
			if(reset==1){
				for(int i=0; i<16; i++){
					regArr[i]=0;
				}
			}
			else{
				PORTA = ( (regArr[reg2]<< 4) | regArr[reg1]%16)%256;
				if(prevclk==1 && currclk == 0){

					PORTD = PORTD & 0b01111111;
					_delay_ms(1000);
					if(regwrite == 1){

						regArr[muxout] = writeData;

						PORTD = PORTD | (1<<7);
						_delay_ms(1000);

					}

					PORTD = PORTD & 0b01111111;
					_delay_ms(1000);
					
				}
			}
		}
		_delay_ms(10);
	}
}
//Register File