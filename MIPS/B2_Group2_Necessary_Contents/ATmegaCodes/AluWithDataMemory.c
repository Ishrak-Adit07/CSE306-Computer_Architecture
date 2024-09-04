#include <avr/io.h>
#define F_CPU 1000000
#include <util/delay.h>

//ALU
#define AND 0x00
#define OR 0x01
#define ADD 0x02
#define SUB 0x03
#define NOR 0x04
#define SLT 0x05
#define SLL 0x06
#define SRL 0x07

//Data memory
unsigned char dataMemoryArray[16];

int main(void)
{

	//Enabling PORTC for I/O
	MCUCSR|=(1<<JTD);
	MCUCSR|=(1<<JTD);

	DDRA = 0xF8; //Selection_bits for ALU(0-2) ALU_Output->(3-6) Zero->7
	DDRB = 0x00; // ALU-> Input_A->(0-3) Instruction(0-3)->(4-7)

	DDRC=0x00;  //Clock, Control, Write Data
	DDRD=0xFE; //ALUSrc->0

	PORTA = 0x00; //Default ALU output
	
	//For ALU
	//unsigned char input = 0;
	unsigned char a = 0;
	unsigned char b = 0;
	unsigned char s = 0;
	unsigned char zero = 0;
	unsigned char ALU_out = 0;
	unsigned char m = 0;

	//For data memory
	int currentClock = 0;
	int previousClock = 0;
	int memToReg;
	int memread;
	int memwrite;
	int writedata;
	int alusrc;
	unsigned int dm_address;
	unsigned int data_out;
	//unsigned int b;
	//unsigned int d;

	//Final output
	unsigned int regWriteData = 0;

	for(int i=0; i<16; i++){
		dataMemoryArray[i]=i+1;
	}
	/* Replace with your application code */
	while (1)
	{

		//input = PINB;

		a = PINB & 0x0F; //ALU_a
		m = PINB & 0xF0; //Instruction(0-3)
		m = (m >> 4) & (0x0F);
		
		//input = PINA;
		s = PINA & 0x07; //ALU_selections

		//b = PIND;
		//All data memory inputs
		//writedata = b>>4;
		writedata = PINC%16; //Data write data
		alusrc = PIND & 0x01; //ALUsrc
		if(alusrc == 0)
		{
			b = writedata;
		}
		else if(alusrc == 1)
		{
			b = m;
		}
		//d = PINC;
		previousClock = currentClock;
		memToReg = (PINC & 0b10000000)>>7;
		currentClock = (PINC & 0b01000000)>>6;
		memread = (PINC & 0b00100000)>>5;
		memwrite= (PINC & 0b00010000)>>4;
		//PINC inputs -> memToReg(7), clock(6), memread(5), memwrite(4), writedata(3-0)

		//ALU operations
		if(s == AND)
		{
			ALU_out = a & b;
		}
		else if(s == OR)
		{
			ALU_out = a | b;
		}
		else if(s == ADD)
		{
			ALU_out = a + b;
		}
		else if(s == SUB)
		{
			ALU_out = a - b;
		}
		else if(s == NOR)
		{
			ALU_out = ~(a | b);
		}
		else if(s == SLT)
		{
			if(a < b)
			{
				ALU_out = 0x0F;
			}
			else
			{
				ALU_out = 0x00;
			}
		}
		else if(s == SLL)
		{
			ALU_out = a << b;
		}
		else if(s == SRL)
		{
			ALU_out = a >> b;
		}

		//Alu_out and Zero
		ALU_out = ALU_out & 0x0F;
		if(ALU_out == 0x00)
		{
			zero = 1;
		}
		else
		{
			zero = 0;
		}
		
		dm_address = ALU_out;
		data_out = 0;
		if(memread) {
			data_out = dataMemoryArray[dm_address];
		}

		//dm_address = ALU_out;
		//data_out = dataMemoryArray[dm_address];

		if(previousClock==1 && currentClock==0){

			if(memwrite==1){
				dataMemoryArray[dm_address] = writedata;
			}
			_delay_ms(100);
		}

		if(memToReg == 1){
			regWriteData = data_out;
		}
		else{
			regWriteData = ALU_out;
		}


		//Show Alu output
		PORTA = (PORTA & 0x07) | (zero << 7) | (regWriteData << 3);
		PORTD = (regWriteData<<4);
		//output = (zero << 7) | (ALU_out<<3);
		//PORTA = output;
		_delay_ms(10);
	}
}