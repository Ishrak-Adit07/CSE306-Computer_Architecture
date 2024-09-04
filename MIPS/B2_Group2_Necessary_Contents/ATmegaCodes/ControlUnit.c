#include <avr/io.h>
#define F_CPU 1000000
#include <util/delay.h>

// generated based on our provided sequence



#define add 0x02
#define addi 0x09
#define sub 0x01
#define subi 0x08
#define and 0x0C
#define andi 0x0B
#define or 0x03
#define ori 0x00
#define sll 0x06
#define srl 0x07
#define nor 0x0E
#define lw 0x05
#define sw 0x0F
#define beq 0x04
#define bneq 0x0A
#define j 0x0D


// defining our operations for producing ALUOP and selectors for each and every mux . ALUOP is not needed actually . This part is done in our circuit


int controlBitsMemory[16] = {0x029,0x423,
	0x422,0x421,
	0x203,0x07a,
	0x02e,0x02f,
	0x02b,0x02a,
	0x103,0x028,
	0x420,0x800,
0x424,0x08a};

//For branch logic
int zero;
int beqStatus = 0;
int bneqStatus = 0;
int branchDecision = 0;

int main(void)
{
	// To use PC2,3,4,5 pins for general I/O operations, JTAG must be disabled.(Enable JTD twice)
	MCUCSR |= (1<<JTD);
	MCUCSR |= (1<<JTD);
	
	DDRA=0x00; // Taking in zero flag (4), opcode (3-0)
	DDRB=0XFF; // Display upper 8 bits of control flags
	DDRD=0XFF; // Display lower 4 bits of control flags
	//DDRC=0X00; //Display lower 4 bits of control flags
	
	PORTB = 0b00000000; //
	PORTD = 0b00000000; //



	while (1)
	{
		unsigned int opCode = PINA & 0b00001111;

		int controlFlags_upper_8 = controlBitsMemory[opCode]/16; //msb 8 bit of 12 bit controls in integer value
		PORTB = controlFlags_upper_8 & 0b11111111;
		int controlFlags_lower_4 = controlBitsMemory[opCode]%16; //lsb 4 bit of 12 bit controls in integer value

		//Branch decision logic
		zero = (PINA & 0b00010000) >> 4;
		beqStatus = (controlFlags_upper_8 & 0b00100000) >> 5;
		bneqStatus = (controlFlags_upper_8 & 0b00010000) >> 4;

		if(beqStatus == 1 && zero == 1){
			branchDecision = 1;
		}
		else if(bneqStatus == 1 && zero == 0){
			branchDecision = 1;
		}
		else{
			branchDecision = 0;
		}

		PORTD = ((controlFlags_lower_4<<4) & 0b11110000)|( ( (branchDecision<<3) & 0b00001111));

		_delay_ms(10);
	}
    
}
//Control flags generation and branch decisions