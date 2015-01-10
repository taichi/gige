package io.gige;

import java.util.List;
import java.util.Map;

@TestAnnotation
public class TestSource<T> {

	int aaa;

	String[] bbb;

	List<T> ccc;

	Map<String, List<Integer>> map;

	public TestSource() {
	}

	protected TestSource(final int aaa) {
		this.aaa = aaa;
	}

	TestSource(final String... bbb) {
		this.bbb = bbb;
	}

	TestSource(final List<T> ccc) {
		this.ccc = ccc;
	}

	TestSource(Map<String, List<Integer>> map) {
		this.map = map;
	}

	void aaa() {
	}

	void aaa(int aaa) {
		this.aaa = aaa;
	}

	public void setAaa(final int aaa) {
		this.aaa = aaa;
	}

	protected void setBbb(final String... bbb) {
		this.bbb = bbb;
	}

	void setCcc(final List<T> ccc) {
		this.ccc = ccc;
	}

	public static <T> TestSource<T> of(List<Map<String, T>> ccc) {
		return null;
	}

}
