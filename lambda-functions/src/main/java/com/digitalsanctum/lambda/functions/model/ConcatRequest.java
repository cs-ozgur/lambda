package com.digitalsanctum.lambda.functions.model;

import java.util.Objects;
import java.util.StringJoiner;

public class ConcatRequest {

    private String firstName;
    private String lastName;

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ConcatRequest that = (ConcatRequest) o;

        return Objects.equals(this.firstName, that.firstName) &&
            Objects.equals(this.lastName, that.lastName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(firstName, lastName);
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", this.getClass().getSimpleName() + "[", "]")
            .add("firstName = " + firstName)
            .add("lastName = " + lastName)
            .toString();
    }
}
