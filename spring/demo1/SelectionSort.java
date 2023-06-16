public class SelectionSort {
    public static void selectionSort(Comparable[] array) {

        for (int i = 0; i < array.length; i++) {
            int min = i;
            for (int j = i + 1; j < array.length; j++) {
                if (isLess(array[j], array[min])) {
                    min = j;
                }
            }
            swap(array, i, min);
        }
    }

    //returns true if Comparable j is less than min
    private static boolean isLess(Comparable j, Comparable min) {
        int comparison = j.compareTo(min);
        return  comparison< 0;
    }

    private static void swap(Comparable[] array, int i, int j) {
        Comparable temp = array[i];
        array[i] = array[j];
        array[j] = temp;
    }

    public static <E> void printArray(E[] array) {
        for (int i = 0; i < array.length; i++) {
            System.out.print(array[i] + " ");
        }
    }

    // Check if array is sorted
    public static boolean isSorted(Comparable[] array) {
        for (int i = 1; i < array.length; i++) {
            if (isLess(array[i], array[i - 1])) {
                return false;
            }
        }
        return true;
    }

    public static void main(String[] args) {
        Integer[] intArray = { 34, 17, 23, 35, 45, 9, 1 };
        System.out.println("Unsorted Array: ");
        printArray(intArray);
        System.out.println("\nIs intArray sorted? "
                + isSorted(intArray));

        selectionSort(intArray);
        System.out.println("\nSelection sort:");
        printArray(intArray);
        System.out.println("\nIs intArray sorted? "
                + isSorted(intArray));

        String[] stringArray = { "z", "g", "c", "o", "a",
                "@", "b", "A", "0", "." };
        System.out.println("\n\nUnsorted Array: ");
        printArray(stringArray);

        System.out.println("\n\nSelection sort:");
        selectionSort(stringArray);
        printArray(stringArray);
    }
}