package com.bughunt.domain;

public class MethodVO {
	private String name;
	private String className;
	private boolean superClass;
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getClassName() {
		return className;
	}
	
	public void setClassName(String className) {
		this.className = className;
	}

	public boolean isSuperClass() {
		return superClass;
	}

	public void setSuperClass(boolean superClass) {
		this.superClass = superClass;
	}

	@Override
	public String toString() {
		return "MethodVO [name=" + name + ", className=" + className + "]";
	}
}
