package com.example.zippy.classes;

import org.jetbrains.annotations.NotNull;

public class Test {
    private String testTitle;
    private String questionPdfUrl;
    private String markSheetCsvUrl;
    private Long totalMark;
    private Double convertTo;

    public Test(){}

    public Test(String testTitle, Long totalMarks, Double convertTo, String questionPdfUrl) {
        this.testTitle = testTitle;
        this.questionPdfUrl = questionPdfUrl;
        this.markSheetCsvUrl = "";
        this.totalMark = totalMarks;
        this.convertTo = convertTo;
    }

    public Test(String testTitle, Long totalMark, Double convertTo) {
        this.testTitle = testTitle;
        this.totalMark = totalMark;
        this.convertTo = convertTo;
        this.questionPdfUrl = "";
        this.markSheetCsvUrl = "";
    }

    public String getTestTitle() {
        return testTitle;
    }

    public void setTestTitle(String testTitle) {
        this.testTitle = testTitle;
    }

    public String getQuestionPdfUrl() {
        return questionPdfUrl;
    }

    public void setQuestionPdfUrl(String questionPdfUrl) {
        this.questionPdfUrl = questionPdfUrl;
    }

    public String getMarkSheetCsvUrl() {
        return markSheetCsvUrl;
    }

    public void setMarkSheetCsvUrl(String markSheetCsvUrl) {
        this.markSheetCsvUrl = markSheetCsvUrl;
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
        return "TestHelper{" +
                "testTitle='" + testTitle + '\'' +
                ", question='" + questionPdfUrl + '\'' +
                ", markSheet='" + markSheetCsvUrl + '\'' +
                ", totalMarks=" + totalMark +
                ", convertTo=" + convertTo +
                '}';
    }
}
