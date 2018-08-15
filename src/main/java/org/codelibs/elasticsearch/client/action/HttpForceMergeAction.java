/*
 * Copyright 2012-2018 CodeLibs Project and the Others.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */
package org.codelibs.elasticsearch.client.action;

import java.io.InputStream;

import org.codelibs.elasticsearch.client.HttpClient;
import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.admin.indices.forcemerge.ForceMergeAction;
import org.elasticsearch.action.admin.indices.forcemerge.ForceMergeRequest;
import org.elasticsearch.action.admin.indices.forcemerge.ForceMergeResponse;
import org.elasticsearch.common.xcontent.XContentParser;

public class HttpForceMergeAction extends HttpAction {

    protected final ForceMergeAction action;

    public HttpForceMergeAction(final HttpClient client, final ForceMergeAction action) {
        super(client);
        this.action = action;
    }

    public void execute(final ForceMergeRequest request, final ActionListener<ForceMergeResponse> listener) {
        client.getCurlRequest(POST, "/_forcemerge", request.indices()).param("max_num_segments", String.valueOf(request.maxNumSegments()))
                .param("only_expunge_deletes", String.valueOf(request.onlyExpungeDeletes())).param("flush", String.valueOf(request.flush()))
                .execute(response -> {
                    if (response.getHttpStatusCode() != 200) {
                        throw new ElasticsearchException("error: " + response.getHttpStatusCode());
                    }
                    try (final InputStream in = response.getContentAsStream()) {
                        final XContentParser parser = createParser(in);
                        final ForceMergeResponse forceMergeResponse = ForceMergeResponse.fromXContent(parser);
                        listener.onResponse(forceMergeResponse);
                    } catch (final Exception e) {
                        listener.onFailure(e);
                    }
                }, listener::onFailure);
    }
}
