package com.example.zippy.helper;

import org.jetbrains.annotations.NotNull;

public class TestHelperClass {
    String testTitle;
    String questionPdfUrl;
    String markSheet;
    Long totalMark;
    Double convertTo;

    public TestHelperClass(){}

    public TestHelperClass(String testTitle, Long totalMarks, Double convertTo, String question) {
        this.testTitle = testTitle;
        this.questionPdfUrl = question;
        this.markSheet = "";
        this.totalMark = totalMarks;
        this.convertTo = convertTo;
    }

    public TestHelperClass(String testTitle, Long totalMark, Double convertTo) {
        this.testTitle = testTitle;
        this.totalMark = totalMark;
        this.convertTo = convertTo;
        this.questionPdfUrl = "";
        this.markSheet = "";
    }

    public String getTestTitle() {
        return testTitle;
    }

    public void setTestTitle(String testTitle) {
        this.testTitle = testTitle;
    }

    public String getQuestion() {
        return questionPdfUrl;
    }

    public void setQuestion(String question) {
        this.questionPdfUrl = question;
    }

    public String getMarkSheet() {
        return markSheet;
    }

    public void setMarkSheet(String markSheet) {
        this.markSheet = markSheet;
    }

    public Long getTotalMark() {
        return totalMark;
    }

    public void setTotalMark(Long totalMark) {
        this.totalMark = totalMark;
    }

    public Double getConvertTo() {
        return convertTo;
    }

    public void setConvertTo(Double convertTo) {
        this.convertTo = convertTo;
    }

    @Override
    public @NotNull String toString() {
        return "TestHelperClass{" +
                "testTitle='" + testTitle + '\'' +
                ", question='" + questionPdfUrl + '\'' +
                ", markSheet='" + markSheet + '\'' +
                ", totalMarks=" + totalMark +
                ", convertTo=" + convertTo +
                '}';
    }
}
