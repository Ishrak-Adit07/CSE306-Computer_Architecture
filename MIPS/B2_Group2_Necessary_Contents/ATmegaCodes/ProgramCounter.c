#include <avr/io.h>
#define F_CPU 1000000
#include <util/delay.h>

int main(void)
{

	DDRA=0XFF;  //Instruction address out
	DDRB=0x00;  //Jump address in
	DDRC=0xFF;  //Next Instructions address through PORTC //Not necessary
	DDRD=0x00;  //Clock, reset, control flags -> branch
	
	MCUCSR|=(1<<JTD);
	MCUCSR|=(1<<JTD);
	
	unsigned char pc = 0;
	unsigned char branchOffset;
	unsigned char jumpAddress;
	unsigned char nextPC = 0;
	int previousClock = 0;
	int currentClock = 0;
	int reset =0;
	int branchControl = 0;
	int jumpControl = 0;
	int clockBlockingCount = 0;
	
	//PC Relay
	while (1) {
		previousClock = currentClock;
		char b = PIND;  //Getting clock, reset and control flags
		
		currentClock = ( b & 0b00000001);  //PD0
		reset = ( b & 0b00000010) >> 1;  //PD1
		branchControl = ( b & 0b00000100) >> 2;  //PD2
		jumpControl = ( b & 0b00001000 ) >> 3;  //PD3
		
		nextPC = pc + 1;  //By default
		PORTC = (10000000) | (nextPC & 0x7F); //Clock from PINC7
		
		if(reset == 1){
			PORTA = 0x00;
			PORTC = 0x00;
		}
		
		else{
			if(previousClock==0 && currentClock==1){ //clock blocking count should be verified here

				PORTC = (nextPC & 0x7F); //No clock from PINC7

				_delay_ms(100);

			}

			PORTC = (10000000) | (nextPC & 0x7F); //Clock from PINC7

			if(previousClock==1 && currentClock==0){ //clock blocking count should be verified here

				if(jumpControl == 1){

					jumpAddress = PINB;
					nextPC = jumpAddress;

				}

				else if(branchControl == 1){
					
					unsigned char branchOffsetLowerFour = ( PIND & 0b11110000) >> 4;  //PD7, PD6, PD5, PD4

					unsigned char boSign = ( branchOffsetLowerFour & 0b00001000 ) >> 3; // boSign = branch offset Sign bit
					branchOffset = (boSign<<7) | (boSign<<6) | (boSign<<5) | (boSign<<4) | branchOffsetLowerFour; //Sign extension

					nextPC = nextPC + branchOffset;
					
				}

				if(clockBlockingCount == 0){
					pc = nextPC;
					clockBlockingCount = clockBlockingCount + 10;
				}

				PORTA = pc;
				//_delay_ms(100);

			}
		}
		
		_delay_ms(10);
		if(clockBlockingCount > 0) clockBlockingCount = clockBlockingCount + 10;
		if(clockBlockingCount == 1000){
			clockBlockingCount = 0;
		}
	}
}
//PC counter with next PC counter(PC+1 or PC+1+branch offset)