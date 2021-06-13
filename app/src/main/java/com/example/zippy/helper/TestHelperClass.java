package com.example.zippy.helper;

public class TestHelperClass {
    private String testTitle, question;
    private Long totalMarks;
    private Long convertTo;

    public TestHelperClass(String testTitle, String question, Long totalMarks, Long convertTo) {
        this.testTitle = testTitle;
        this.question = question;
        this.totalMarks = totalMarks;
        this.convertTo = convertTo;
    }

    public String getTestTitle() {
        return testTitle;
    }

    public void setTestTitle(String testTitle) {
        this.testTitle = testTitle;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public Long getTotalMarks() {
        return totalMarks;
    }

    public void setTotalMarks(Long totalMarks) {
        this.totalMarks = totalMarks;
    }

    public Long getConvertTo() {
        return convertTo;
    }

    public void setConvertTo(Long convertTo) {
        this.convertTo = convertTo;
    }

    @Override
    public String toString() {
        return "TestHelperClass{" +
                "testTitle='" + testTitle + '\'' +
                ", question='" + question + '\'' +
                ", totalMarks=" + totalMarks +
                ", convertTo=" + convertTo +
                '}';
    }
}
