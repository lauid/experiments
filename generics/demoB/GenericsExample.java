/**
 * $ javac GenericsExample.java
 * $ java GenericsExample
 */
public class GenericsExample {
    public static void main(String[] args) {
        GenericClass<String> stringBox = new GenericClass<>("Hello");
        String str = stringBox.getValue();
        System.out.println(str);

        GenericClass<Integer> intBox = new GenericClass<Integer>(123);
        Integer intV = intBox.getValue();
        System.out.println(intV);


        Double[] doubleArray = {1.1, 2.3, 3.3, 4.4, 5.5};
        GenericMethod.printArray(doubleArray);

        String[] stringArray = {"one", "two", "three", "four", "five"};
        GenericMethod.printArray(stringArray);
    }

    //泛型类
    public static class GenericClass<T> {
        private T value;

        public GenericClass(T value) {
            this.value = value;
        }

        public T getValue() {
            return value;
        }
    }

    //泛型方法
    public static class GenericMethod {
        public static <T> void printArray(T[] arr) {
            for (T element : arr) {
                System.out.print(element + " ");
            }
            System.out.println();
        }
    }
}

