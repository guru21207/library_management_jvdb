import java.time.LocalDate;

public class BorrowRecordDetail {

    private final int recordId;
    private final String memberId;
    private final String memberName;
    private final String bookId;
    private final String bookTitle;
    private final LocalDate issueDate;
    private final LocalDate returnDate;
    private final Integer daysKept;
    private final double fine;

    public BorrowRecordDetail(int recordId, String memberId, String memberName,
                              String bookId, String bookTitle,
                              LocalDate issueDate, LocalDate returnDate,
                              Integer daysKept, double fine) {
        this.recordId = recordId;
        this.memberId = memberId;
        this.memberName = memberName != null ? memberName : "Unknown Member";
        this.bookId = bookId;
        this.bookTitle = bookTitle != null ? bookTitle : "Unknown Book";
        this.issueDate = issueDate;
        this.returnDate = returnDate;
        this.daysKept = daysKept;
        this.fine = fine;
    }

    public int getRecordId() {
        return recordId;
    }

    public String getMemberId() {
        return memberId;
    }

    public String getMemberName() {
        return memberName;
    }

    public String getBookId() {
        return bookId;
    }

    public String getBookTitle() {
        return bookTitle;
    }

    public LocalDate getIssueDate() {
        return issueDate;
    }

    public LocalDate getReturnDate() {
        return returnDate;
    }

    public Integer getDaysKept() {
        return daysKept;
    }

    public double getFine() {
        return fine;
    }

    public boolean isReturned() {
        return returnDate != null;
    }

    @Override
    public String toString() {
        return "Record #" + recordId + " | " + memberName + " borrowed '" + bookTitle +
                "' on " + issueDate + (isReturned() ? (" (Returned, Fine: ₹" + String.format("%.2f", fine) + ")") : " (Active Loan)");
    }
}
