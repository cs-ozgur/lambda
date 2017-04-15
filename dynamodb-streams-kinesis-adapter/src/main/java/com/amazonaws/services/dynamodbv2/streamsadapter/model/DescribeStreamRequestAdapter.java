/*
 * Copyright 2014-2015 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Amazon Software License (the "License").
 * You may not use this file except in compliance with the License.
 * A copy of the License is located at
 *
 * http://aws.amazon.com/asl/
 *
 * or in the "license" file accompanying this file. This file is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */
package com.amazonaws.services.dynamodbv2.streamsadapter.model;

import com.amazonaws.services.dynamodbv2.model.DescribeStreamRequest;

/**
 * Container for the parameters to the DescribeStream operation.
 */
public class DescribeStreamRequestAdapter extends DescribeStreamRequest {

    private com.amazonaws.services.kinesis.model.DescribeStreamRequest internalRequest;

    /**
     * Constructs a new request using an Amazon Kinesis object.
     *
     * @param request Instance of Amazon Kinesis DescribeStreamRequest
     */
    public DescribeStreamRequestAdapter(com.amazonaws.services.kinesis.model.DescribeStreamRequest request) {
        internalRequest = request;
    }

    /**
     * @return The shard ID of the shard to start with for the stream description.
     */
    @Override
    public String getExclusiveStartShardId() {
        return internalRequest.getExclusiveStartShardId();
    }

    /**
     * @param exclusiveStartShardId The shard ID of the shard to start with for the stream description.
     */
    @Override
    public void setExclusiveStartShardId(String exclusiveStartShardId) {
        internalRequest.setExclusiveStartShardId(exclusiveStartShardId);
    }

    /**
     * @param exclusiveStartShardId The shard ID of the shard to start with for the stream description.
     * @return A reference to this updated object so that method calls can be chained together.
     */
    @Override
    public DescribeStreamRequest withExclusiveStartShardId(String exclusiveStartShardId) {
        internalRequest.setExclusiveStartShardId(exclusiveStartShardId);
        return this;
    }

    /**
     * @return The maximum number of shards to return.
     */
    @Override
    public Integer getLimit() {
        return internalRequest.getLimit();
    }

    /**
     * @param limit The maximum number of shards to return.
     */
    @Override
    public void setLimit(Integer limit) {
        internalRequest.setLimit(limit);
    }

    /**
     * @param limit The maximum number of shards to return.
     * @return A reference to this updated object so that method calls can be chained together.
     */
    @Override
    public DescribeStreamRequest withLimit(Integer limit) {
        internalRequest.setLimit(limit);
        return this;
    }

    /**
     * @return The ARN of the stream to describe.
     */
    @Override
    public String getStreamArn() {
        return internalRequest.getStreamName();
    }

    /**
     * @param streamArn The ARN of the stream to describe.
     */
    @Override
    public void setStreamArn(String streamArn) {
        internalRequest.setStreamName(streamArn);
    }

    /**
     *
     * @param streamArn The ARN of the stream to describe.
     * @return A reference to this updated object so that method calls can be chained together.
     */
    @Override
    public DescribeStreamRequest withStreamArn(String streamArn) {
        internalRequest.setStreamName(streamArn);
        return this;
    }

}
