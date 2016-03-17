/*
 * Copyright 2016 Francisco Franco & Liam Spradlin
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package liam.franco.selene.modules;

// builder class for the About activity items because
// we herd you like Abouts so we put an About in an About so you can About while you About
public class About {
    private String title;
    private String url;

    private About(Builder builder) {
        this.title = builder.title;
        this.url = builder.url;
    }

    public static class Builder {
        private String title;
        private String url;

        public Builder setTitle(String title) {
            this.title = title;
            return this;
        }

        public Builder setUrl(String url) {
            this.url = url;
            return this;
        }

        public About build() {
            return new About(this);
        }
    }

    public String getTitle() {
        return title;
    }

    public String getUrl() {
        return url;
    }
}
