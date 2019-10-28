public class Test1
	extends Animal
	implements Cat, Dog {
	public static void main(String[] args) {
		Dog d = new Dog();
		Cat c = new Cat();
		IDog dImp = d;
		ICat cImp = c;

		d.speak();
		dImp.woof();

		c.speak();
		cImp.meow();
	}
}

class Animal {
	public void speak() { System.out.println("virtual blah"); }
}

class Dog extends Animal implements IDog {
	public void speak() { System.out.println("virtual woof"); }
	public void woof() { System.out.println("interface woof"); }
}

class Cat extends Animal implements ICat {
	public void speak() { System.out.println("virtual meow"); }
	public void meow() { System.out.println("interface meow"); }
}

interface IDog {
	public void woof();
}

interface ICat {
	public void meow();
}
