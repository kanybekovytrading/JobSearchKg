package job.search.kg.payment;

import lombok.Getter;

/**
 * ✅ Конфигурация всех банков Кыргызстана с поддержкой вывода по номеру телефона
 */
@Getter
public enum BankConfig {

    BAKAI_24(
            "cec5df87-daa0-475d-8aca-e68b9b253ad2",
            "4398",
            "Бакай24 - по номеру телефона",
            10,
            99999,
            false
    ),

    DEMIR_BANK(
            "e63b44d3-69b5-4335-b9f0-49faf85e9d56",
            "5221",
            "Демир Банк - по номеру телефона",
            10,
            50000,
            false
    ),


    OPTIMA_BANK(
            "07dbb2ef-ff5c-413f-a2ec-5c439ec5714d",
            "5428",
            "Оптима Банк по номеру телефона",
            15,
            95000,
            false
    ),

    ELDIK_BANK(
            "0ab40b4d-a06c-42a2-ba18-15e2240f40a3",
            "4019",
            "Элдик Банк по номеру телефона (РСК)",
            101,
            69999,
            false
    ),

    ELCART(
            "b4e55a02-5daa-4cd4-874a-b812c8a3904e",
            "4417",
            "Элкарт - по номеру телефона",
            20,
            15000,
            false
    ),

    KICB(
            "5f9b81a9-aef2-4027-81f3-08d8796e6c68",
            "4910",
            "KICB Банк - по номеру телефона",
            5,
            99999,
            false
    ),

    AIYL_BANK(
            "f3174f11-f399-42e0-9450-b9ee15531946",
            "4543",
            "Айыл Банк по номеру телефона",
            5,
            50000,
            false
    ),

    BAI_TUSHUM(
            "61d21503-fbfb-4f6c-bae0-2582613b8630",
            "5523",
            "Пополнение счета Бай-Тушум по номеру телефона",
            20,
            69999,
            false
    ),

    SIMBANK(
            "96be1e30-9965-4842-8a59-231512484d8d",
            "5475",
            "Simbank по номеру телефона",
            50,
            69000,
            false
    ),

    TULPAR(
            "f89ba382-74d3-4d9d-94e5-1e2125ae3814",
            "3982",
            "Карта Тулпар по номеру телефона",
            1,
            90000,
            false
    ),

    MBANK(
            "averspay-elqr-mbank",
            null,  // У MBank нет serviceCode
            "MBank - по номеру телефона",
            1,
            99000,
            true   // ✅ Требует transactionType!
    ),
    KKB(
            "51bfbad6-e668-4c79-b010-7d1534eb2616",
            "4425",
            "Кыргызкоммерцбанк по номеру телефона",
            20,
            69999,
            false
    );
//    BAKAI_24(
//            "c1f045ca-0f9f-48bf-8a2f-2566dc55bb45",
//                    "4398",
//                    "Бакай24 - по номеру телефона",
//                    10,
//                    99999,
//                    false
//    ),
//

//
//    OPTIMA_BANK(
//            "7132d037-abc3-4f82-a621-c4c01c874f51",
//                    "5428",
//                    "Оптима Банк по номеру телефона",
//                    15,
//                    95000,
//                    false
//    ),
//
//    ELDIK_BANK(
//            "0ab40b4d-a06c-42a2-ba18-15e2240f40a3",
//                    "4019",
//                    "Элдик Банк по номеру телефона (РСК)",
//                    101,
//                    69999,
//                    false
//    ),
//
//    ELCART(
//            "25dd4456-b390-4c89-9287-8fb4782e0a5c",
//                    "4417",
//                    "Элкарт - по номеру телефона",
//                    20,
//                    15000,
//                    false
//    ),
//
//    KICB(
//            "5f9b81a9-aef2-4027-81f3-08d8796e6c68",
//                    "4910",
//                    "KICB Банк - по номеру телефона",
//                    5,
//                    99999,
//                    false
//    ),
//
//    AIYL_BANK(
//            "1e67f6f3-abc6-41b1-a243-06a1a6090026",
//                    "4543",
//                    "Айыл Банк по номеру телефона",
//                    5,
//                    50000,
//                    false
//    ),
//
//    BAI_TUSHUM(
//            "49922384-ea51-4218-b8ae-d3331048261e",
//                    "5523",
//                    "Пополнение счета Бай-Тушум по номеру телефона",
//                    20,
//                    69999,
//                    false
//    ),
//
//    SIMBANK(
//            "9a097ba1-6eb4-421a-bee2-a174bbf94576",
//                    "5475",
//                    "Simbank по номеру телефона",
//                    50,
//                    69000,
//                    false
//    ),
//
//    TULPAR(
//            "f89ba382-74d3-4d9d-94e5-1e2125ae3814",
//                    "3982",
//                    "Карта Тулпар по номеру телефона",
//                    1,
//                    90000,
//                    false
//    ),
//
//    MBANK(
//            "averspay-elqr-mbank",
//                    null,  // У MBank нет serviceCode
//                    "MBank - по номеру телефона",
//                    1,
//                    99000,
//                    true   // ✅ Требует transactionType!
//    ),
//    KKB(
//            "0d03508e-0858-4606-8b80-fca4677e37fa",
//                    "4425",
//                    "Кыргызкоммерцбанк по номеру телефона",
//                    20,
//                    69999,
//                    false
//    );


    private final String serviceId;      // ID сервиса в Finik API
    private final String serviceCode;    // Код сервиса (для поля "service")
    private final String name;           // Название банка
    private final int minAmount;         // Минимальная сумма
    private final int maxAmount;
    private final boolean requiresTransactionType;         // Максимальная сумма

    BankConfig(String serviceId, String serviceCode, String name, int minAmount, int maxAmount, boolean requiresTransactionType) {
        this.serviceId = serviceId;
        this.serviceCode = serviceCode;
        this.name = name;
        this.minAmount = minAmount;
        this.maxAmount = maxAmount;
        this.requiresTransactionType = requiresTransactionType;
    }

    /**
     * Найти банк по serviceId
     */
    public static BankConfig findByServiceId(String serviceId) {
        for (BankConfig bank : values()) {
            if (bank.serviceId.equals(serviceId)) {
                return bank;
            }
        }
        return null;
    }

    /**
     * Найти банк по названию (частичное совпадение)
     */
    public static BankConfig findByName(String name) {
        for (BankConfig bank : values()) {
            if (bank.name.toLowerCase().contains(name.toLowerCase())) {
                return bank;
            }
        }
        return null;
    }

    /**
     * Валидация суммы для банка
     */
    public boolean isAmountValid(int amount) {
        return amount >= minAmount && amount <= maxAmount;
    }
}