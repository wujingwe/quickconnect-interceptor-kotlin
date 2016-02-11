QuickConnectInterceptor
=======================

An OkHttp application interceptor for Synology Quick Connect

What is OkHttp application interceptor?
---------------------------------------

A powerful tool introduced by [OkHttp][okhttp]. We use it to resolve real IP address before sending request to Synology NAS server, detailed information can be found [here][interceptor].


What is Synology Quick Connect?
-------------------------------

[Synology Inc. (Chinese: 群暉科技)][synology] is a Taiwanese corporation that specializes in network attached storage (NAS) appliances.
QuickConnect is designed to make connecting Synology NAS servers easy and quick, even NAS is behind NAT. You can find the white paper [here][quickconnect].

What is Kotlin(programming language)
------------------------------------

[Kotlin][kotlin] is a modern programming language that runs on the Java Virtual Machine.
It provides lots of advantages compared with Java and is considerable to be a replacement(especially for Android which can only support Java 1.7 currently.)

* Null-safety
* Lambda expressions
* Extension functions
* Properties and more...

How to use
----------

- Add jar in buid.gradle
```groovy
compile project (':quickconnect')
```

- Add QuickConnectInterceptor when creating OkHttpClient
```kotlin
val client = OkHttpClient.Builder()
				.addInterceptor(QuickConnectInterceptor())
				.build()
```

- Send a request to Synology NAS server. 
To use QuickConnect, you need to have a QuickConnect ID. To compose a HTTP request, replace hostname with QuickConnect ID.
For example, if your QuickConnect ID is "**dsm**" and want to send a pingpong request(webman/pingpong.cgi?action=cors), the request should be looked like:
```kotlin
val quickConnectId = "dsm"
val request = Request.Builder()
				.url(HttpUrl.parse("http://$quickConnectId/webman/pingpong.cgi?action=cors"))
				.build();
client.newCall(request).execute();
```


License
-------

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and 
    limitations under the License.


[okhttp]: http://square.github.io/okhttp/
[interceptor]: https://github.com/square/okhttp/wiki/Interceptors
[synology]: https://www.synology.com
[kotlin]: https://kotlinlang.org/
[quickconnect]: https://global.download.synology.com/download/Document/WhitePaper/Synology_QuickConnect_White_Paper.pdf
