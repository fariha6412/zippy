package com.example.zippy.helper;

public class TestHelperClass {
    String testTitle, question;
    Long totalMarks;
    Double convertTo;

    public TestHelperClass(String testTitle, String question, Long totalMarks, Double convertTo) {
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

    public Double getConvertTo() {
        return convertTo;
    }

    public void setConvertTo(Double convertTo) {
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
