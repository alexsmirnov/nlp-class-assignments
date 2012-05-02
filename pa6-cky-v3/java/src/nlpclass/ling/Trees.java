package nlpclass.ling;

import nlpclass.util.Filter;

import java.io.IOException;
import java.io.PushbackReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.*;

/**
 * Tools for displaying, reading, and modifying trees.
 *
 * @author Dan Klein
 * @author Paul Baumstarck (added GENIA and BioIE readers 2008.4)
 */
public class Trees {

	public static interface TreeTransformer<E> {
		Tree<E> transformTree(Tree<E> tree);
	}

	public static class FunctionNodeStripper implements TreeTransformer<String> {
		public Tree<String> transformTree(Tree<String> tree) {
			String transformedLabel = tree.getLabel();
			int cutIndex = transformedLabel.indexOf('-');
			int cutIndex2 = transformedLabel.indexOf('=');
			if (cutIndex2 > 0 && (cutIndex2 < cutIndex || cutIndex == -1))
				cutIndex = cutIndex2;
			cutIndex2 = transformedLabel.indexOf('^');
			if (cutIndex2 > 0 && (cutIndex2 < cutIndex || cutIndex == -1))
				cutIndex = cutIndex2;
			cutIndex2 = transformedLabel.indexOf(':');
			if (cutIndex2 > 0 && (cutIndex2 < cutIndex || cutIndex == -1))
				cutIndex = cutIndex2;

			if (cutIndex > 0 && !tree.isLeaf()) {
				transformedLabel = new String(transformedLabel.substring(0,
						cutIndex));
			}
			if (tree.isLeaf()) {
				return new Tree<String>(transformedLabel);
			}
			List<Tree<String>> transformedChildren = new ArrayList<Tree<String>>();
			for (Tree<String> child : tree.getChildren()) {
				transformedChildren.add(transformTree(child));
			}
			return new Tree<String>(transformedLabel, transformedChildren);
		}
	}

	public static class EmptyNodeStripper implements TreeTransformer<String> {
		public Tree<String> transformTree(Tree<String> tree) {
			String label = tree.getLabel();
			if (label.equals("-NONE-")) {
				return null;
			}
			if (tree.isLeaf()) {
				return new Tree<String>(label);
			}
			List<Tree<String>> children = tree.getChildren();
			List<Tree<String>> transformedChildren = new ArrayList<Tree<String>>();
			for (Tree<String> child : children) {
				Tree<String> transformedChild = transformTree(child);
				if (transformedChild != null)
					transformedChildren.add(transformedChild);
			}
			if (transformedChildren.size() == 0)
				return null;
			return new Tree<String>(label, transformedChildren);
		}
	}

	public static class XOverXRemover<E> implements TreeTransformer<E> {
		public Tree<E> transformTree(Tree<E> tree) {
			E label = tree.getLabel();
			List<Tree<E>> children = tree.getChildren();
			while (children.size() == 1 && !children.get(0).isLeaf()
					&& label.equals(children.get(0).getLabel())) {
				children = children.get(0).getChildren();
			}
			List<Tree<E>> transformedChildren = new ArrayList<Tree<E>>();
			for (Tree<E> child : children) {
				transformedChildren.add(transformTree(child));
			}
			return new Tree<E>(label, transformedChildren);
		}
	}

	public static class StandardTreeNormalizer implements
			TreeTransformer<String> {
		EmptyNodeStripper emptyNodeStripper = new EmptyNodeStripper();
		XOverXRemover<String> xOverXRemover = new XOverXRemover<String>();
		FunctionNodeStripper functionNodeStripper = new FunctionNodeStripper();

		public Tree<String> transformTree(Tree<String> tree) {
			tree = functionNodeStripper.transformTree(tree);
			tree = emptyNodeStripper.transformTree(tree);
			tree = xOverXRemover.transformTree(tree);
			return tree;
		}
	}
	
	public static class TreeReader {
		public static String ROOT_LABEL = "ROOT";
		
		PushbackReader in;
		Tree<String> nextTree;

		public boolean hasNext() {
			return (nextTree != null);
		}

		public Tree<String> next() {
			if (!hasNext())
				throw new NoSuchElementException();
			Tree<String> tree = nextTree;
			nextTree = readRootTree();
			return tree;
		}
		
		public Tree<String> readRootTree() {
			throw new RuntimeException("readRootTree() undefined.");
		}

		public int peek() throws IOException {
			int ch = in.read();
			in.unread(ch);
			return ch;
		}

		public String readLabel() throws IOException {
			readWhiteSpace();
			return readText();
		}
		
		public Tree<String> readLeaf() throws IOException {
			String label = readText();
			return new Tree<String>(label);
		}
		
		public String readText() throws IOException {
			StringBuilder sb = new StringBuilder();
			int ch = in.read();
			while (!isWhiteSpace(ch) && !isLeftParen(ch) && !isRightParen(ch)) {
				sb.append((char) ch);
				ch = in.read();
			}
			in.unread(ch);
			//      System.out.println("Read text: ["+sb+"]");
			return sb.toString().intern();
		}
		
		public void readLeftParen() throws IOException {
			//      System.out.println("Read left.");
			readWhiteSpace();
			int ch = in.read();
			if (!isLeftParen(ch)) {
				throw new RuntimeException("Format error reading tree.");
			}
		}

		public void readRightParen() throws IOException {
			//      System.out.println("Read right.");
			readWhiteSpace();
			int ch = in.read();
			if (!isRightParen(ch)) {
				throw new RuntimeException("Format error reading tree.");
			}
		}

		public void readWhiteSpace() throws IOException {
			int ch = in.read();
			while (isWhiteSpace(ch)) {
				ch = in.read();
			}
			in.unread(ch);
		}

		public boolean isWhiteSpace(int ch) {
			return (ch == ' ' || ch == '\t' || ch == '\f' || ch == '\r' || ch == '\n');
		}

		public boolean isLeftParen(int ch) {
			return ch == '(';
		}

		public boolean isRightParen(int ch) {
			return ch == ')';
		}
		
		public boolean isSemicolon(int ch) {
			return ch == ';';
		}

		public void remove() {
			throw new UnsupportedOperationException();
		}
	}
	
	public static class BioIETreeReader extends TreeReader implements Iterator<Tree<String>> {
		
		public Tree<String> readRootTree() {
			try {
				for (;;) {
					readCommentsAndWhiteSpace();
					if (!isLeftParen(peek()))
						return null;
					in.read(); // (
					String str = readText();
					if ( str.equals("SENT") )
						break; // read the sentence
					else if ( str.equals("SEC") ) {
						// read in the section, "("-less tree, then throw it away
						readTree(false);
					} else {
						//System.out.println("Failed: "+str);
						return null; // reading error
					}
				}
				// We have stripped off "(SENT", so read a "("-less tree.
				return new Tree<String>(ROOT_LABEL, Collections.singletonList(readTree(false)));
			} catch (IOException e) {
				throw new RuntimeException("Error reading tree."+e.toString());
			}
		}

		private Tree<String> readTree(boolean matchparen) throws IOException {
			if ( matchparen )
				readLeftParen();
			String label = readColonizedLabel();
			//System.out.println("  "+label);
			List<Tree<String>> children = readChildren();
			readRightParen();
			return new Tree<String>(label, children);
		}
		
		public String readColonizedLabel() throws IOException {
			readWhiteSpace();
			String ret = readText();
			int i = ret.indexOf(":");
			if ( i == -1 )
				return ret;
			else
				return ret.substring(0,i);
		}
		
		private List<Tree<String>> readChildren() throws IOException {
			readWhiteSpace();
			if (!isLeftParen(peek()))
				return Collections.singletonList(readLeaf());
			else
				return readChildList();
		}

		private List<Tree<String>> readChildList() throws IOException {
			List<Tree<String>> children = new ArrayList<Tree<String>>();
			readWhiteSpace();
			while (!isRightParen(peek())) {
				children.add(readTree(true));
				readWhiteSpace();
			}
			return children;
		}
				
		private void readCommentsAndWhiteSpace() throws IOException {
			int ch;
			for (;;) {
				readWhiteSpace();
				if ( !isSemicolon(peek()) )
					return;
				// read a line
				ch = in.read();
				while ( ch != '\n' )
					ch = in.read();
			}
		}

		public BioIETreeReader(Reader in) {
			this.in = new PushbackReader(in);
			nextTree = readRootTree();
		}
	}

	public static class PennTreeReader extends TreeReader implements Iterator<Tree<String>> {
		
		public Tree<String> readRootTree() {
			try {
				readWhiteSpace();
				if (!isLeftParen(peek()))
					return null;
				return readTree(true);
			} catch (IOException e) {
				throw new RuntimeException("Error reading tree.");
			}
		}

		private Tree<String> readTree(boolean isRoot) throws IOException {
			readLeftParen();
			String label = readLabel();
			if (label.length() == 0 && isRoot)
				label = ROOT_LABEL;
			List<Tree<String>> children = readChildren();
			readRightParen();
			return new Tree<String>(label, children);
		}

		private List<Tree<String>> readChildren() throws IOException {
			readWhiteSpace();
			if (!isLeftParen(peek()))
				return Collections.singletonList(readLeaf());
			return readChildList();
		}

		private List<Tree<String>> readChildList() throws IOException {
			List<Tree<String>> children = new ArrayList<Tree<String>>();
			readWhiteSpace();
			while (!isRightParen(peek())) {
				children.add(readTree(false));
				readWhiteSpace();
			}
			return children;
		}

		public PennTreeReader(Reader in) {
			this.in = new PushbackReader(in);
			nextTree = readRootTree();
		}
	}

	public static class GENIATreeReader extends TreeReader implements Iterator<Tree<String>> {
	
		public Tree<String> readRootTree() {
			try {
				readWhiteSpace();
				if (!isLeftParen(peek()))
					return null;
				return new Tree<String>(ROOT_LABEL, Collections.singletonList(readTree(false)));
			} catch (IOException e) {
				throw new RuntimeException("Error reading tree.");
			}
		}

		private Tree<String> readTree(boolean isRoot) throws IOException {
			readLeftParen();
			String label = readLabel();
			if (label.length() == 0 && isRoot)
				label = ROOT_LABEL;
			List<Tree<String>> children = readChildren();
			readRightParen();
			return new Tree<String>(label, children);
		}

		private List<Tree<String>> readChildren() throws IOException {
			List<Tree<String>> children = new ArrayList<Tree<String>>();
			readWhiteSpace();
			while (!isRightParen(peek())) {
				if ( isLeftParen(peek()) ) {
					children.add(readTree(false));
				} else {
					Tree<String> ret = readSlashLabel();
					if ( ret != null )
						children.add(ret);
				}
				readWhiteSpace();
			}
			return children;
		}
		
		private Tree<String> readSlashLabel() throws IOException {
			String label = readText();
			int i = label.lastIndexOf("/");
			if ( i == -1 ) return null;
			while ( i > 0 && label.charAt(i-1) == '\\' ) {
				i = label.lastIndexOf("/",i-1);
			}
			return new Tree<String>(label.substring(i+1),
					Collections.singletonList(new Tree<String>( label.substring(0,i).replaceAll("\\\\\\/","\\/") )));
		}

		public GENIATreeReader(Reader in) {
			this.in = new PushbackReader(in);
			nextTree = readRootTree();
		}
	}

	/**
	 * Renderer for pretty-printing trees according to the Penn Treebank indenting
	 * guidelines (mutliline).  Adapted from code originally written by Dan Klein
	 * and modified by Chris Manning.
	 */
	public static class PennTreeRenderer {

		/**
		 * Print the tree as done in Penn Treebank merged files. The formatting
		 * should be exactly the same, but we don't print the trailing whitespace
		 * found in Penn Treebank trees. The basic deviation from a bracketed
		 * indented tree is to in general collapse the printing of adjacent
		 * preterminals onto one line of tags and words.  Additional complexities
		 * are that conjunctions (tag CC) are not collapsed in this way, and that
		 * the unlabeled outer brackets are collapsed onto the same line as the next
		 * bracket down.
		 */
		public static <L> String render(Tree<L> tree) {
			StringBuilder sb = new StringBuilder();
			renderTree(tree, 0, false, false, false, true, sb);
			sb.append('\n');
			return sb.toString();
		}

		/**
		 * Display a node, implementing Penn Treebank style layout
		 */
		private static <L> void renderTree(Tree<L> tree, int indent,
				boolean parentLabelNull, boolean firstSibling,
				boolean leftSiblingPreTerminal, boolean topLevel,
				StringBuilder sb) {
			// the condition for staying on the same line in Penn Treebank
			boolean suppressIndent = (parentLabelNull
					|| (firstSibling && tree.isPreTerminal()) || (leftSiblingPreTerminal
					&& tree.isPreTerminal() && (tree.getLabel() == null || !tree
					.getLabel().toString().startsWith("CC"))));
			if (suppressIndent) {
				sb.append(' ');
			} else {
				if (!topLevel) {
					sb.append('\n');
				}
				for (int i = 0; i < indent; i++) {
					sb.append("  ");
				}
			}
			if (tree.isLeaf() || tree.isPreTerminal()) {
				renderFlat(tree, sb);
				return;
			}
			sb.append('(');
			sb.append(tree.getLabel());
			renderChildren(tree.getChildren(), indent + 1,
					tree.getLabel() == null
							|| tree.getLabel().toString() == null, sb);
			sb.append(')');
		}

		private static <L> void renderFlat(Tree<L> tree, StringBuilder sb) {
			if (tree.isLeaf()) {
				sb.append(tree.getLabel().toString());
				return;
			}
			sb.append('(');
			sb.append(tree.getLabel().toString());
			sb.append(' ');
			sb.append(tree.getChildren().get(0).getLabel().toString());
			sb.append(')');
		}

		private static <L> void renderChildren(List<Tree<L>> children,
				int indent, boolean parentLabelNull, StringBuilder sb) {
			boolean firstSibling = true;
			boolean leftSibIsPreTerm = true; // counts as true at beginning
			for (Tree<L> child : children) {
				renderTree(child, indent, parentLabelNull, firstSibling,
						leftSibIsPreTerm, false, sb);
				leftSibIsPreTerm = child.isPreTerminal();
				// CC is a special case
				if (child.getLabel() != null
						&& child.getLabel().toString().startsWith("CC")) {
					leftSibIsPreTerm = false;
				}
				firstSibling = false;
			}
		}
	}

	public static void main(String[] args) {
		PennTreeReader reader = new PennTreeReader(
				new StringReader(
						"((S (NP (DT the) (JJ quick) (JJ brown) (NN fox)) (VP (VBD jumped) (PP (IN over) (NP (DT the) (JJ lazy) (NN dog)))) (. .)))"));
		Tree<String> tree = reader.next();
		System.out.println(PennTreeRenderer.render(tree));
		System.out.println(tree);
	}

	/**
	 * Splices out all nodes which match the provided filter.
	 *
	 * @param tree
	 * @param filter
	 */
	public static <L> Tree<L> spliceNodes(Tree<L> tree, Filter<L> filter) {
		List<Tree<L>> rootList = spliceNodesHelper(tree, filter);
		if (rootList.size() > 1)
			throw new IllegalArgumentException(
					"spliceNodes: no unique root after splicing");
		if (rootList.size() < 1)
			return null;
		return rootList.get(0);
	}

	private static <L> List<Tree<L>> spliceNodesHelper(Tree<L> tree,
			Filter<L> filter) {
		List<Tree<L>> splicedChildren = new ArrayList<Tree<L>>();
		for (Tree<L> child : tree.getChildren()) {
			List<Tree<L>> splicedChildList = spliceNodesHelper(child, filter);
			splicedChildren.addAll(splicedChildList);
		}
		if (filter.accept(tree.getLabel()))
			return splicedChildren;
		return Collections.singletonList(new Tree<L>(tree.getLabel(),
				splicedChildren));
	}

	/**
	 * Prunes out all nodes which match the provided filter (and nodes which dominate only pruned nodes).
	 *
	 * @param tree
	 * @param filter
	 */
	public static <L> Tree<L> pruneNodes(Tree<L> tree, Filter<L> filter) {
		return pruneNodesHelper(tree, filter);
	}

	private static <L> Tree<L> pruneNodesHelper(Tree<L> tree, Filter<L> filter) {
		if (filter.accept(tree.getLabel()))
			return null;
		List<Tree<L>> prunedChildren = new ArrayList<Tree<L>>();
		for (Tree<L> child : tree.getChildren()) {
			Tree<L> prunedChild = pruneNodesHelper(child, filter);
			if (prunedChild != null)
				prunedChildren.add(prunedChild);
		}
		if (prunedChildren.isEmpty() && !tree.isLeaf())
			return null;
		return new Tree<L>(tree.getLabel(), prunedChildren);
	}

}
