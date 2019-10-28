public class Test2
	: Animal, Cat, Dog {
	public static void Main(String[] args) {
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

internal class Animal {
	public void speak() { Console.WriteLine("virtual blah"); }
}

internal class Dog : Animal, IDog {
	public void speak() { Console.WriteLine("virtual woof"); }
	public void woof() { Console.WriteLine("interface woof"); }
}

internal class Cat : Animal, ICat {
	public void speak() { Console.WriteLine("virtual meow"); }
	public void meow() { Console.WriteLine("interface meow"); }
}

internal interface IDog {
	public void woof();
}

internal interface ICat {
	public void meow();
}
