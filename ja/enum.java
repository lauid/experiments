enum ProductSpuStatusEnum {
    STATUS_A(1),
    STATUS_B(2),
    STATUS_C(3);

    private final int status;

    ProductSpuStatusEnum(int status) {
        this.status = status;
    }

    public int getStatus() {
        return status;
    }
}

public class Main {
    public static void main(String[] args) {
        int[] statusArray = Arrays.stream(ProductSpuStatusEnum.values())
                .mapToInt(ProductSpuStatusEnum::getStatus)
                .toArray();

        System.out.println(Arrays.toString(statusArray));
    }
}

