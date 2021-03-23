package com.example.MeetBarber;

public class ReviewDetails {
    private String ReviewImageUrl;
    private String ReviewCustomerName;
    private  String ReviewDate;
    private String RecviewRatingValue;
    private String ReviewComments;

    public ReviewDetails() {
    }

    public ReviewDetails(String reviewImageUrl, String reviewCustomerName, String reviewDate, String recviewRatingValue, String reviewComments) {
        ReviewImageUrl = reviewImageUrl;
        ReviewCustomerName = reviewCustomerName;
        ReviewDate = reviewDate;
        RecviewRatingValue = recviewRatingValue;
        ReviewComments = reviewComments;
    }

    public String getReviewComments() {
        return ReviewComments;
    }

    public void setReviewComments(String reviewComments) {
        ReviewComments = reviewComments;
    }

    public String getReviewImageUrl() {
        return ReviewImageUrl;
    }

    public void setReviewImageUrl(String reviewImageUrl) {
        ReviewImageUrl = reviewImageUrl;
    }

    public String getReviewCustomerName() {
        return ReviewCustomerName;
    }

    public void setReviewCustomerName(String reviewCustomerName) {
        ReviewCustomerName = reviewCustomerName;
    }

    public String getReviewDate() {
        return ReviewDate;
    }

    public void setReviewDate(String reviewDate) {
        ReviewDate = reviewDate;
    }

    public String getRecviewRatingValue() {
        return RecviewRatingValue;
    }

    public void setRecviewRatingValue(String recviewRatingValue) {
        RecviewRatingValue = recviewRatingValue;
    }
}
