package com.example.zippy.helper;

public class TestHelperClass {
    String testTitle, question, gradingScale, markSheet;
    Long totalMark;
    Double convertTo;

    public TestHelperClass(String testTitle, Long totalMarks, Double convertTo, String question) {
        this.testTitle = testTitle;
        this.question = question;
        this.gradingScale = "";
        this.markSheet = "";
        this.totalMark = totalMarks;
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

    public String getGradingScale() {
        return gradingScale;
    }

    public void setGradingScale(String gradingScale) {
        this.gradingScale = gradingScale;
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
    public String toString() {
        return "TestHelperClass{" +
                "testTitle='" + testTitle + '\'' +
                ", question='" + question + '\'' +
                ", gradingScale='" + gradingScale + '\'' +
                ", markSheet='" + markSheet + '\'' +
                ", totalMarks=" + totalMark +
                ", convertTo=" + convertTo +
                '}';
    }
}
