/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package compilersimulation.instructiontypes.groups.types;

import compilersimulation.instructiontypes.groups.LoadAndStore;

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
	accumulator = super.memory.fetchAtIndex(operand);
    } 
}
