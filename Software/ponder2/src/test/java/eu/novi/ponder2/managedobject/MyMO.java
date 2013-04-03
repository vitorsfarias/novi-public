package eu.novi.ponder2.managedobject;

import eu.novi.ponder2.ManagedObject;
import eu.novi.ponder2.apt.Ponder2op;
import eu.novi.ponder2.objects.P2Object;

public class MyMO implements ManagedObject {

	protected String name = null;
	protected int age = -1;

	@Ponder2op("testStatic:")
	protected static P2Object testStatic(String aName) {
		System.out.println("Static called");
		return P2Object.create(aName + aName);
	}

	@Ponder2op("create:at:")
	public MyMO(String name, int age) {
		super();
		this.name = name;
		this.age = age;
		System.out.println("MyMO.MyMO(): my name is " + this.name
				+ " and my age is " + this.age);

	}

	@Ponder2op("print")
	public void p2_operation_print_name() {
		System.out.println("MyMO.printName(): My name is " + name
				+ " and my age is " + age);
	}

	@Ponder2op("name")
	protected String getName() {
		return name;
	}

	@Ponder2op("name:")
	public String p2_operation_set_name(String name) {
		String oldName = this.name;
		this.name = name;
		System.out.println("MyMO.setName(): My name is " + oldName);
		System.out.println("MyMO.setName(): Setting my name to " + this.name);
		return oldName;
	}

	@Ponder2op("setage:")
	public int p2_operation_set_age(int age) {
		int oldAge = this.age;
		this.age = age;
		System.out.println("MyMO.p2_operation_set_age(): My old age is "
				+ oldAge);
		System.out.println("MyMO.p2_operation_set_age() My new age is "
				+ this.age);
		return oldAge;

	}

	@Ponder2op("age")
	protected int getAge() {
		return age;
	}

	@Ponder2op("name:age:")
	public void p2_operation_set_name_age(String name, int age) {

		p2_operation_set_name(name);
		p2_operation_set_age(age);
	}

}
