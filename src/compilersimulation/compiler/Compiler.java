/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package compilersimulation.compiler;

import compilersimulation.exceptions.OutOfMemoryException;
import compilersimulation.simpletronhardware.Disk;
import java.io.FileReader;
import java.io.IOException;
import java.io.StreamTokenizer;
import java.util.Arrays;

import compilersimulation.compiler.InfixToPostfixConverter;
import customgenericdatastructures.StackCustom;
import java.io.FileNotFoundException;

/**
 *
 * @author markoc
 */
public class Compiler {
    private EntryTable[] symbolTable = new EntryTable[100];
    private int[] flags = new int[100];
    private int frontLocation = 00;
    private int backLocation = 99;
    private int instructionCounter = 0;
    private int[] SML = new int[100];
    private String filePathSML;
    
    public EntryTable[] compile(String filePath) throws IOException{
	this.filePathSML = "src/compilersimulation/simpletronhardware/SML/"
		+filePath.replaceFirst("^.+?(?=\\w+[.]?\\w+$)", "").replaceFirst("[.]\\w+$", ".sml");
	
	firstPass(filePath);
	secondPass(filePath);
	return symbolTable;
    }
    
    private void firstPass(String filePath) throws FileNotFoundException, IOException{
	StreamTokenizer st = new StreamTokenizer(new FileReader(filePath));
	InfixToPostfixConverter itpc = new InfixToPostfixConverter();
	
	st.eolIsSignificant(true); // treat end-of-lines as tokens
	st.ordinaryChar('/');
	Arrays.fill(flags,-1);
	while (st.nextToken() != StreamTokenizer.TT_EOF) {
	    // ignore rem statements
	    if(st.sval != null && st.sval.equals("rem"))
		while(st.nextToken() != StreamTokenizer.TT_EOL)
		{/* ignore rem */}

	    switch (st.ttype) {
		case StreamTokenizer.TT_EOL:
		    break;

		case StreamTokenizer.TT_NUMBER:
			pushEntry((int)st.nval,'L');
		    break;

		case StreamTokenizer.TT_WORD:
		    int symbol, location;
		    switch(st.sval){
			case "input":
			    st.nextToken(); // move to variable
			    symbol = (int)st.sval.charAt(0);
			    insertIntoSML(1000 + backLocation);
			    
			    pushEntry(symbol,'V');
			    break;
			    
			case "print":
			    st.nextToken(); // move to variable
			    symbol = (int)st.sval.charAt(0);
			    location = getMemoryLocation(symbol,'V');
			    if(location >= 0)
			    {
				insertIntoSML(1100 + location);
			    }
			    break;
			    
			case "let":
			    st.nextToken();
			    pushEntry((int)st.sval.charAt(0), 'V');
			    location = getMemoryLocation((int)st.sval.charAt(0), 'V'); // temporary store final location
			    
			    if(st.nextToken() != StreamTokenizer.TT_EOL)
			    {
				StringBuffer sb = new StringBuffer();

				while(st.nextToken() != StreamTokenizer.TT_EOL){

				    switch(st.ttype){
					case StreamTokenizer.TT_NUMBER:
					    pushEntry((int)st.nval, 'C');
					    sb.append(getMemoryLocation((int)st.nval, 'C'));
					    break;
					case StreamTokenizer.TT_WORD:
					    sb.append(getMemoryLocation((int)st.sval.charAt(0), 'V'));
					    break;
					default:

					    if(isOperator((char)st.ttype))
						sb.append(" ").append((char)st.ttype).append(" ");
					    else
						sb.append((char)st.ttype);
				    }

				}
				if(sb.length() == 2)
				{
				    insertIntoSML(2000 + Integer.parseInt(sb.toString()));
				    insertIntoSML(2100 + location);
				}
				else
				    // load temporary location to accumulator
				    convertExpressionToSML(itpc.toPostfix(sb), location);
			    }
			    else {
				insertIntoSML(2100 + location);
			    }
			    
			    break;
			    
			case "if":
			    StringBuffer comparisonSymbol = new StringBuffer();
			    boolean first = true;
			    
			    while(st.nextToken() != StreamTokenizer.TT_EOL && st.sval == null || !isCommand(st.sval)){
				switch(st.ttype){
				    case StreamTokenizer.TT_NUMBER:
					pushEntry((int)st.nval,'C');
					first = outputComparisonOperaterCodes(first, getMemoryLocation((int)st.nval, 'C'));
					break;
				    case StreamTokenizer.TT_WORD:
					symbol = (int)st.sval.charAt(0);
					first = outputComparisonOperaterCodes(first, getMemoryLocation(symbol,'V'));
					break;
				    default:
					comparisonSymbol.append(String.valueOf((char)st.ttype));
				}
			    }
			    // expected goto command location number
			    st.nextToken();
			    // get branching location
			    outputBranchingCodes(comparisonSymbol, (int)st.nval);
			    break;
			    
			case "goto":
			    st.nextToken();
			    symbol = (int)st.nval;
			    location = getMemoryLocation(symbol, 'L');
			    if(location >= 0)
				insertIntoSML(4000 + location);
			    else
			    {
				flags[instructionCounter] = symbol;
				insertIntoSML(4000);
			    }
			    break;
			    
			case "end":
			    insertIntoSML(4300);
			    break;
			default:
			    break;
		    }
		    break;	
		default:
		    break;
	    }
	}
    }
    
    public void secondPass(String filePath){
	Disk disk = new Disk();
	for(int f=0; f<flags.length; f++){
	    if(flags[f]>=0){
		int location = getMemoryLocation(flags[f], 'L');
		if(location == -1)
		    throw new IllegalArgumentException("Line "+flags[f]+" is missing.");
		SML[f] = SML[f]+location;
	    }
	}
	
	disk.openFile(this.filePathSML);
	for (int sml : SML) {
	    if(sml==0) break;
	    disk.printToFile((sml > 0) ? "+"+sml : String.valueOf(sml));
	}
	
	disk.closeFile();
    }
    
    public void print(){
	for(EntryTable et : symbolTable){
	    if(et == null)
		return;
	    System.out.printf("%-2s %-1c %-2d%n",et.getSymbol(),et.getType(),et.getLocation());
	}

    }
    
    private boolean isCommand(String command){
	return command.equals("input") || command.equals("print") || command.equals("goto") || command.equals("end") || command.equals("let") || command.equals("if");
    }
    
    private int getMemoryLocation(int symbol, char type){
	for(EntryTable et : symbolTable){
	    if(et == null)
		return -1;
	    else if(et.symbol == symbol && et.type == type)
		return et.location;
	}
	return -1;
    }
    
    private void pushEntry(int symbol, char type){
	int searchLocation = getMemoryLocation(symbol, type);
	if(searchLocation == -1)
	{
	    symbolTable[frontLocation++] = new EntryTable(symbol,type,(type == 'L') ? instructionCounter : backLocation--);
	}
    }
    
    private boolean outputComparisonOperaterCodes(boolean first, int memoryLocation){
	if(first) // if first element: load
	{
	    if(isRedundantInstruction(memoryLocation))
		insertIntoSML(2000+memoryLocation);
	    first = !first;
	}
	else // if second element: subtract
	    insertIntoSML(3100+memoryLocation);
	
	return first;
    }
    
    private void outputBranchingCodes(StringBuffer comparisonSymbol, int value){
	int branchingLocation = getMemoryLocation(value, 'L');
	
	for(int i=0;i<comparisonSymbol.length();i++){
	    if(branchingLocation == -1)
		flags[instructionCounter] = value;
	    
	    int location = (branchingLocation == -1) ? 0 : branchingLocation;
	    
	    switch(comparisonSymbol.toString().charAt(i)){
		case '<':
		    insertIntoSML(4100 + location);
		    break;
		case '>':
		    insertIntoSML(4000 + location);
		    break;
		case '=':
		    insertIntoSML(4200 + location);
		    return;
	    }
	}
    }
    
    private void insertIntoSML(int code){
	if(instructionCounter == SML.length)
	    throw new OutOfMemoryException();
	SML[instructionCounter] = code;
	instructionCounter++;
    }
    
    private boolean isOperator(char operator){
	return operator == '+' || operator == '-' || operator == '*' || operator == '/';
    }
    
    private int operatorCode(String operator){
	switch(operator){
	    case "+":
		return 3000;
	    case "-":
		return 3100;
	    case "/":
		return 3200;
	    case "*":
		return 3300;
	    default:
		throw new ArithmeticException("Operator not allowed.");
	}
    }
    
    private int convertExpressionToSML(StringBuffer postfix, int resultLocation){
	StackCustom<Integer> stack = new StackCustom<>();
	int tempLocation = backLocation--;
	String[] tp = postfix.toString().split("\\s");

	for(int i=0; i<tp.length;i++){
	    String token = tp[i];
	    if(!isOperator(token.charAt(0)))
		stack.push(Integer.parseInt(token));
	    else
	    {
		int y = stack.pop();
		int x = stack.pop();
		int oCode = operatorCode(token);
		
		if(isRedundantInstruction(x))
		    insertIntoSML(2000 + x); // load first integer
		
		insertIntoSML(oCode + y); // use second integer and produce result
		
		if(i<tp.length-1) // store new value to temp location until second last run
		    insertIntoSML(2100 + tempLocation);
		
		stack.push(tempLocation); // push temporary location onto stack
	    }
	}
	
	// store store result to final location/variable
	insertIntoSML(2100 + resultLocation);
			    
	return stack.pop(); // return temp location
    }

    public String getFilePathSML(){
	return this.filePathSML;
    }
    
    public boolean isRedundantInstruction(int memoryLocation){
	return (memoryLocation != (SML[instructionCounter-1] % 100) ||  (SML[instructionCounter-1] - memoryLocation) == 1000);
    }
}

