package io.gige;

import java.io.BufferedReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.junit.Before;
import org.junit.Test;

public class MatcherTest {

	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void test() throws Exception {
		ASTNode left = createNode("src/test/java/io/gige/TestSource.java");
		ASTNode right = createNode("src/test/java/io/gige/TestSource.jav_");
	}

	ASTNode createNode(String filepath) throws Exception {
		Path path = Paths.get(filepath);
		long length = path.toFile().length();
		try (BufferedReader br = Files.newBufferedReader(path)) {
			char[] buff = new char[(int) length];
			br.read(buff);
			ASTParser parser = ASTParser.newParser(AST.JLS8);
			parser.setSource(buff);
			return parser.createAST(null);
		}
	}

}
