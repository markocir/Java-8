/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package compilersimulation.instructiontypes;

/**
 *
 * @author markoc
 */
public class Load extends LoadAndStore{
    
    public Load(int code){
	super(code);
    }
    
    @Override
    public void executeInstruction() throws Exception{
	accumulator = memory.fetchAtIndex(operand);
    } 
}