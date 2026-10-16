package com.example.data.database

import com.example.data.model.AccountGroupEntity
import com.example.data.model.BankAccountEntity
import com.example.data.model.CashDeskEntity
import com.example.data.model.ChequeEntity
import com.example.data.model.GeneralAccountEntity
import com.example.data.model.InventoryMovementEntity
import com.example.data.model.InvoiceEntity
import com.example.data.model.InvoiceItemEntity
import com.example.data.model.JournalEntryEntity
import com.example.data.model.JournalItemEntity
import com.example.data.model.PartyEntity
import com.example.data.model.ProductEntity
import com.example.data.model.SubsidiaryAccountEntity
import com.example.data.model.WarehouseEntity
import com.example.util.JalaliDate

object DatabaseSeeder {

    suspend fun seedDefaultData(db: AppDatabase) {
        val today = JalaliDate.now()
        val todayStr = today.toStandardString()

        // 1. Account Groups (گروه‌های اصلی حسابداری)
        val groups = listOf(
            AccountGroupEntity(1, "دارایی‌های جاری", "DEBIT"),
            AccountGroupEntity(2, "دارایی‌های غیرجاری", "DEBIT"),
            AccountGroupEntity(3, "بدهی‌های جاری", "CREDIT"),
            AccountGroupEntity(4, "بدهی‌های بلندمدت", "CREDIT"),
            AccountGroupEntity(5, "حقوق صاحبان سهام", "CREDIT"),
            AccountGroupEntity(6, "درآمدها و فروش", "CREDIT"),
            AccountGroupEntity(7, "بهای تمام شده کالای فروش رفته", "DEBIT"),
            AccountGroupEntity(8, "هزینه‌های عمومی و اداری", "DEBIT")
        )
        db.accountingDao().insertAccountGroups(groups)

        // 2. General Accounts (حساب‌های کل)
        val generals = listOf(
            GeneralAccountEntity(101, 1, "موجودی نقد و بانک", "DEBIT"),
            GeneralAccountEntity(102, 1, "حساب‌ها و اسناد دریافتنی تجاری", "DEBIT"),
            GeneralAccountEntity(103, 1, "موجودی کالا و انبار", "DEBIT"),
            GeneralAccountEntity(201, 2, "اموال، ماشین‌آلات و تجهیزات", "DEBIT"),
            GeneralAccountEntity(301, 3, "حساب‌ها و اسناد پرداختنی تجاری", "CREDIT"),
            GeneralAccountEntity(302, 3, "پیش‌دریافت‌ها و مالیات پرداختنی", "CREDIT"),
            GeneralAccountEntity(501, 5, "سرمایه اولیه موسسه", "CREDIT"),
            GeneralAccountEntity(601, 6, "درآمد حاصل از فروش کالا", "CREDIT"),
            GeneralAccountEntity(701, 7, "بهای تمام شده کالای فروش رفته", "DEBIT"),
            GeneralAccountEntity(801, 8, "هزینه‌های حقوق، کرایه و ملزومات", "DEBIT")
        )
        db.accountingDao().insertGeneralAccounts(generals)

        // 3. Subsidiary Accounts (حساب‌های معین)
        val subsidiaries = listOf(
            SubsidiaryAccountEntity(10101, 101, "صندوق‌های فروشگاه و دخل", "DEBIT"),
            SubsidiaryAccountEntity(10102, 101, "حساب‌های بانکی و پوز POS", "DEBIT"),
            SubsidiaryAccountEntity(10103, 101, "تنخواه‌گردان مدیریت", "DEBIT"),
            SubsidiaryAccountEntity(10201, 102, "بدهکاران تجاری (مشتریان نسیه)", "DEBIT"),
            SubsidiaryAccountEntity(10202, 102, "اسناد دریافتنی (چک‌های اشخاص)", "DEBIT"),
            SubsidiaryAccountEntity(10301, 103, "موجودی انبار مرکزی", "DEBIT"),
            SubsidiaryAccountEntity(10302, 103, "موجودی انبار فروشگاه", "DEBIT"),
            SubsidiaryAccountEntity(30101, 301, "بستانکاران تجاری (تامین‌کنندگان کالا)", "CREDIT"),
            SubsidiaryAccountEntity(30102, 301, "اسناد پرداختنی (چک‌های صادره)", "CREDIT"),
            SubsidiaryAccountEntity(50101, 501, "سرمایه ثبت شده شرکت گلی", "CREDIT"),
            SubsidiaryAccountEntity(60101, 601, "فروش نقدی و اعتباری اجناس", "CREDIT"),
            SubsidiaryAccountEntity(70101, 701, "قیمت تمام شده اجناس فروش رفته", "DEBIT"),
            SubsidiaryAccountEntity(80101, 801, "هزینه‌های پرسنلی و حقوق", "DEBIT"),
            SubsidiaryAccountEntity(80102, 801, "کرایه حمل و هزینه‌های بسته‌بندی", "DEBIT")
        )
        db.accountingDao().insertSubsidiaryAccounts(subsidiaries)

        // 4. Warehouses (انبارها)
        val warehouses = listOf(
            WarehouseEntity(1, "W101", "انبار مرکزی تهران", "مهندس محمدی", "خیابان آزادی، پلاک ۱۲", "021-66778899"),
            WarehouseEntity(2, "W102", "انبار فروشگاه و دخل", "خانم اکبری", "بازار بزرگ، سرای امید", "021-55443322"),
            WarehouseEntity(3, "W103", "انبار ضایعات و مرجوعی", "آقای حسینی", "خیابان فداییان اسلام", "021-33221100")
        )
        db.inventoryDao().insertWarehouses(warehouses)

        // 5. Products (کالاها با واحد اصلی و فرعی و قیمت‌گذاری چندسطحی)
        val products = listOf(
            ProductEntity(
                id = 1,
                code = "PRD-101",
                barcode = "6260123456781",
                name = "روغن زیتون فرابکر ۱ لیتری گلی",
                category = "روغن‌ها و چاشنی",
                primaryUnit = "کارتن",
                secondaryUnit = "عدد",
                conversionRate = 12.0,
                currentStockInSecondary = 48.0, // 4 کارتن
                minStockThreshold = 12.0,
                buyPrice = 250000L,
                retailPrice = 320000L,
                wholesalePrice = 285000L,
                partnerPrice = 275000L,
                description = "روغن زیتون طبیعی بکر با اسیدیته زیر ۰.۵ درصد"
            ),
            ProductEntity(
                id = 2,
                code = "PRD-102",
                barcode = "6260123456782",
                name = "برنج طارم هاشمی درجه یک معطر",
                category = "غلات و برنج",
                primaryUnit = "کیسه ۱۰k",
                secondaryUnit = "کیلوگرم",
                conversionRate = 10.0,
                currentStockInSecondary = 120.0, // 12 کیسه
                minStockThreshold = 20.0,
                buyPrice = 110000L,
                retailPrice = 150000L,
                wholesalePrice = 135000L,
                partnerPrice = 128000L,
                description = "برنج اعلای گیلان سورت شده و الک شده"
            ),
            ProductEntity(
                id = 3,
                code = "PRD-103",
                barcode = "6260123456783",
                name = "چای سیلان زرین اعلای گلی (۵۰۰ گرمی)",
                category = "نوشیدنی و دمنوش",
                primaryUnit = "کارتن",
                secondaryUnit = "بسته",
                conversionRate = 20.0,
                currentStockInSecondary = 60.0, // 3 کارتن
                minStockThreshold = 15.0,
                buyPrice = 175000L,
                retailPrice = 225000L,
                wholesalePrice = 198000L,
                partnerPrice = 190000L,
                description = "چای شکسته خارجی با عطر برگاموت طبیعی"
            ),
            ProductEntity(
                id = 4,
                code = "PRD-104",
                barcode = "6260123456784",
                name = "رب گوجه فرنگی غلیظ ۸۰۰ گرمی",
                category = "کنسرویجات",
                primaryUnit = "باکس",
                secondaryUnit = "قوطی",
                conversionRate = 12.0,
                currentStockInSecondary = 8.0, // کمبود موجودی نسبت به نقطه سفارش!
                minStockThreshold = 24.0,
                buyPrice = 62000L,
                retailPrice = 85000L,
                wholesalePrice = 73000L,
                partnerPrice = 70000L,
                description = "رب گوجه بریکس ۲۷ درصد قوطی کلیددار"
            ),
            ProductEntity(
                id = 5,
                code = "PRD-105",
                barcode = "6260123456785",
                name = "شکر سفید تصفیه شده یک کیلویی",
                category = "قند و شکر",
                primaryUnit = "کارتن",
                secondaryUnit = "بسته",
                conversionRate = 24.0,
                currentStockInSecondary = 96.0, // 4 کارتن
                minStockThreshold = 24.0,
                buyPrice = 38000L,
                retailPrice = 49000L,
                wholesalePrice = 43000L,
                partnerPrice = 41500L,
                description = "شکر استاندارد بسته‌بندی نایلونی"
            ),
            ProductEntity(
                id = 6,
                code = "PRD-106",
                barcode = "6260123456786",
                name = "ماکارونی ۷۰۰ گرمی زر مانا",
                category = "غلات و ماکارونی",
                primaryUnit = "کارتن",
                secondaryUnit = "بسته",
                conversionRate = 20.0,
                currentStockInSecondary = 80.0,
                minStockThreshold = 20.0,
                buyPrice = 24000L,
                retailPrice = 33000L,
                wholesalePrice = 28000L,
                partnerPrice = 26500L,
                description = "ماکارونی رشته‌ای ضخامت ۱.۵ غنی شده"
            )
        )
        db.inventoryDao().insertProducts(products)

        // 6. Parties (CRM مشتریان با سقف اعتبار ریالی)
        val parties = listOf(
            PartyEntity(
                id = 1,
                code = "CUS-101",
                name = "هایپرمارکت بزرگ بهارستان",
                companyName = "شرکت پخش بهارستان امید",
                mobile = "09121112233",
                phone = "021-88990011",
                address = "میدان بهارستان، خیابان صفی علی شاه، پلاک ۴۴",
                nationalCode = "10103456789",
                partyType = "CUSTOMER",
                creditLimit = 60000000L, // سقف اعتبار ۶۰ میلیون تومان
                currentBalance = 14500000L, // بدهکار ۱۴.۵ میلیون تومان (در محدوده مجاز)
                notes = "مشتری خوش‌حساب و فعال منطقه ۱۲"
            ),
            PartyEntity(
                id = 2,
                code = "CUS-102",
                name = "سوپرمارکت و پروتئینی آریا",
                companyName = "فروشگاه آریا غرب",
                mobile = "09129876543",
                phone = "021-44332211",
                address = "ستارخان، بعد از پل یادگار، نرسیده به بازارچه سنتی",
                nationalCode = "0078945612",
                partyType = "CUSTOMER",
                creditLimit = 20000000L, // سقف اعتبار ۲۰ میلیون تومان
                currentBalance = 19200000L, // بدهکار ۱۹.۲ میلیون تومان (هشدار: نزدیک به سقف!)
                notes = "نیاز به تسویه حساب قبل از سفارش‌های بعدی بیش از ۱ میلیون"
            ),
            PartyEntity(
                id = 3,
                code = "CUS-103",
                name = "بنکداری و پخش برادران رضایی",
                companyName = "بازرگانی رضایی",
                mobile = "09133334455",
                phone = "031-32214455",
                address = "اصفهان، میدان امام، سرای تحدید",
                nationalCode = "1289945621",
                partyType = "CUSTOMER",
                creditLimit = 150000000L,
                currentBalance = 0L,
                notes = "خرید عمده کارتن و پرداخت با چک‌های معتبر صیادی"
            ),
            PartyEntity(
                id = 4,
                code = "SUP-201",
                name = "صنایع غذایی و کشت و صنعت مهر البرز",
                companyName = "مهر البرز هلدینگ",
                mobile = "09124445566",
                phone = "026-34567890",
                address = "شهرک صنعتی بهارستان، خیابان گلستان ۶",
                nationalCode = "10100987654",
                partyType = "SUPPLIER",
                creditLimit = 0L,
                currentBalance = -32000000L, // بستانکار ۳۲ میلیون تومان (ما بدهکاریم)
                notes = "تامین‌کننده اصلی روغن زیتون و سس‌های ارگانیک"
            ),
            PartyEntity(
                id = 5,
                code = "SUP-202",
                name = "شرکت پخش سراسری غلات خاورمیانه",
                companyName = "غلات خاورمیانه سهامی خاص",
                mobile = "09127778899",
                phone = "021-22334455",
                address = "بلوار میرداماد، برج آرین، طبقه ۷",
                nationalCode = "10101122334",
                partyType = "SUPPLIER",
                creditLimit = 0L,
                currentBalance = -18500000L, // بستانکار ۱۸.۵ میلیون تومان
                notes = "تامین‌کننده برنج طارم و شکر"
            )
        )
        db.partyDao().insertParties(parties)

        // 7. Treasury: Cash Desks & Bank Accounts
        val cashDesks = listOf(
            CashDeskEntity(1, "CSH-01", "صندوق مرکزی و مدیریت", 32500000L, "آقای گلی", "صندوق اصلی مستقر در اتاق مدیریت"),
            CashDeskEntity(2, "CSH-02", "دخل فروشگاه و نقدینگی صندوقدار", 8400000L, "خانم کریمی", "دخل روزانه پایانه فروشگاه")
        )
        db.treasuryDao().insertCashDesks(cashDesks)

        val bankAccounts = listOf(
            BankAccountEntity(
                id = 1,
                bankName = "بانک ملت",
                accountNumber = "4982345120",
                shebaNumber = "IR45012000000004982345120",
                cardNumber = "6104-3378-9876-1234",
                branch = "شعبه بازار بزرگ (کد ۱۲۴)",
                currentBalance = 145000000L,
                hasPos = true,
                posTerminalId = "POS-MLT-9872"
            ),
            BankAccountEntity(
                id = 2,
                bankName = "بانک صادرات ایران",
                accountNumber = "0103456789004",
                shebaNumber = "IR1901900000000103456789004",
                cardNumber = "6037-6919-4455-6677",
                branch = "شعبه میدان شهدا",
                currentBalance = 58200000L,
                hasPos = true,
                posTerminalId = "POS-SDR-5411"
            )
        )
        db.treasuryDao().insertBankAccounts(bankAccounts)

        // 8. Cheques with due date alerts
        val dueIn2Days = today.addDays(2)
        val dueIn5Days = today.addDays(5)
        val overdueDate = today.addDays(-3)

        val cheques = listOf(
            ChequeEntity(
                id = 1,
                chequeNumber = "CHK-9812",
                sayadNumber = "7812903456123456",
                type = "RECEIVABLE", // چک دریافتی از مشتری
                partyId = 1,
                partyName = "هایپرمارکت بهارستان",
                bankName = "بانک ملت",
                branch = "شعبه مرکزی",
                amount = 14500000L,
                issueDateJalali = today.addDays(-20).toStandardString(),
                dueDateJalali = dueIn2Days.toStandardString(),
                dueDateEpochMillis = dueIn2Days.toEpochMillis(),
                status = "NORMAL",
                notes = "بابت تسویه فاکتور فروش شماره ۱۰۴"
            ),
            ChequeEntity(
                id = 2,
                chequeNumber = "CHK-5534",
                sayadNumber = "4321098765432109",
                type = "RECEIVABLE",
                partyId = 2,
                partyName = "سوپرمارکت آریا",
                bankName = "بانک سپه",
                branch = "شعبه ستارخان",
                amount = 8200000L,
                issueDateJalali = today.addDays(-15).toStandardString(),
                dueDateJalali = todayStr, // سررسید امروز!
                dueDateEpochMillis = today.toEpochMillis(),
                status = "NORMAL",
                notes = "چک تضمین خرید نسیه ماه جاری"
            ),
            ChequeEntity(
                id = 3,
                chequeNumber = "CHK-7741",
                sayadNumber = "9988776655443322",
                type = "PAYABLE", // چک پرداختی به تامین‌کننده
                partyId = 4,
                partyName = "صنایع غذایی مهر البرز",
                bankName = "بانک صادرات",
                branch = "شعبه شهدا",
                amount = 22000000L,
                issueDateJalali = today.addDays(-10).toStandardString(),
                dueDateJalali = dueIn5Days.toStandardString(),
                dueDateEpochMillis = dueIn5Days.toEpochMillis(),
                status = "NORMAL",
                notes = "قسط اول خرید عمده روغن زیتون فرابکر"
            ),
            ChequeEntity(
                id = 4,
                chequeNumber = "CHK-1102",
                sayadNumber = "5544332211009988",
                type = "RECEIVABLE",
                partyId = 3,
                partyName = "بنکداری برادران رضایی",
                bankName = "بانک ملی",
                branch = "اصفهان",
                amount = 45000000L,
                issueDateJalali = today.addDays(-35).toStandardString(),
                dueDateJalali = overdueDate.toStandardString(),
                dueDateEpochMillis = overdueDate.toEpochMillis(),
                status = "NORMAL", // سررسید گذشته (هشدار مهم!)
                notes = "پیگیری فوری با آقای رضایی جهت وصول"
            )
        )
        db.treasuryDao().insertCheques(cheques)

        // 9. Initial Journal Entries (سند افتتاحیه و ثبت متوازن)
        val openingVoucher = JournalEntryEntity(
            id = 1,
            voucherNumber = 1,
            jalaliDate = todayStr,
            description = "سند افتتاحیه و استقرار مانده حساب‌های اولیه شرکت گلی",
            isBalanced = true,
            status = "FINAL"
        )
        val voucherId = db.accountingDao().insertJournalEntry(openingVoucher)

        val items = listOf(
            JournalItemEntity(
                voucherId = voucherId,
                generalCode = 101,
                subsidiaryCode = 10101,
                description = "موجودی صندوق مرکزی در بدو افتتاح",
                debit = 40900000L,
                credit = 0L
            ),
            JournalItemEntity(
                voucherId = voucherId,
                generalCode = 101,
                subsidiaryCode = 10102,
                description = "موجودی حساب‌های بانکی ملت و صادرات",
                debit = 203200000L,
                credit = 0L
            ),
            JournalItemEntity(
                voucherId = voucherId,
                generalCode = 103,
                subsidiaryCode = 10301,
                description = "ارزش موجودی اولیه کالای انبار مرکزی",
                debit = 65000000L,
                credit = 0L
            ),
            JournalItemEntity(
                voucherId = voucherId,
                generalCode = 501,
                subsidiaryCode = 50101,
                description = "سرمایه اولیه سهامداران موسسه گلی",
                debit = 0L,
                credit = 309100000L
            )
        )
        db.accountingDao().insertJournalItems(items)

        // 10. Initial Invoices (نمونه فاکتور رسمی ثبت شده)
        val initialInvoice = InvoiceEntity(
            id = 1,
            invoiceNumber = "INV-1001",
            invoiceType = "SALE",
            jalaliDate = todayStr,
            partyId = 1,
            partyName = "هایپرمارکت بزرگ بهارستان",
            partyPhone = "09121112233",
            warehouseId = 1,
            warehouseName = "انبار مرکزی تهران",
            subtotalAmount = 6400000L,
            discountAmount = 400000L,
            taxAmount = 600000L,
            totalAmount = 6600000L,
            paymentType = "POS",
            posAmount = 6600000L,
            isPaid = true,
            notes = "تحویل درب فروشگاه بهارستان - تسویه پایانه کارتخوان"
        )
        val invId = db.invoiceDao().insertInvoice(initialInvoice)
        val invoiceItems = listOf(
            InvoiceItemEntity(
                invoiceId = invId,
                productId = 1,
                productName = "روغن زیتون فرابکر ۱ لیتری گلی",
                barcode = "6260123456781",
                unitName = "کارتن",
                isPrimaryUnit = true,
                quantity = 1.0,
                conversionRate = 12.0,
                quantityInSecondary = 12.0,
                unitPrice = 3420000L, // 12 * 285000
                totalPrice = 3420000L
            ),
            InvoiceItemEntity(
                invoiceId = invId,
                productId = 2,
                productName = "برنج طارم هاشمی درجه یک معطر",
                barcode = "6260123456782",
                unitName = "کیسه ۱۰k",
                isPrimaryUnit = true,
                quantity = 2.0,
                conversionRate = 10.0,
                quantityInSecondary = 20.0,
                unitPrice = 1350000L,
                totalPrice = 2700000L
            )
        )
        db.invoiceDao().insertInvoiceItems(invoiceItems)
    }
}
