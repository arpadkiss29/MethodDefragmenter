package ro.lrg.method.defragmenter.visitors;
import java.util.ArrayList;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.MethodDeclaration;

public class BlockVisitor extends ASTVisitor{
	private final ArrayList<Block> blocks = new ArrayList<>();
	
	public ArrayList<Block> getBlocks() {
		return blocks;
	}
	
	@Override
	public boolean visit(Block block) {
		blocks.add(block);
		return super.visit(block);
	}
	
}
