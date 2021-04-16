/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache license, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the license for the specific language governing permissions and
 * limitations under the license.
 */
package org.apache.logging.log4j.mongodb3;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.categories.Appenders;
import org.apache.logging.log4j.core.junit.LoggerContextRule;
import org.apache.logging.log4j.mongodb3.MongoDb3TestRule.LoggingTarget;
import org.apache.logging.log4j.test.AvailablePortSystemPropertyTestRule;
import org.apache.logging.log4j.test.RuleChainFactory;
import org.bson.Document;
import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.rules.RuleChain;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

/**
 * This class name does NOT end in "Test" in order to only be picked up by {@link Java8Test}.
 */
@Category(Appenders.MongoDb.class)
public class MongoDb3CappedTest {

    private static LoggerContextRule loggerContextTestRule = new LoggerContextRule("log4j2-mongodb-capped.xml");

    private static final AvailablePortSystemPropertyTestRule mongoDbPortTestRule = AvailablePortSystemPropertyTestRule
            .create(MongoDb3TestConstants.SYS_PROP_NAME_PORT);

    private static final MongoDb3TestRule mongoDbTestRule = new MongoDb3TestRule(mongoDbPortTestRule.getName(),
            MongoDb3CappedTest.class, LoggingTarget.NULL);

    @ClassRule
    public static RuleChain ruleChain = RuleChainFactory.create(mongoDbPortTestRule, mongoDbTestRule,
            loggerContextTestRule);

    @Test
    public void test() {
        final Logger logger = LogManager.getLogger();
        logger.info("Hello log");
        try (final MongoClient mongoClient = mongoDbTestRule.getMongoClient()) {
            final MongoDatabase database = mongoClient.getDatabase("test");
            Assert.assertNotNull(database);
            final MongoCollection<Document> collection = database.getCollection("applog");
            Assert.assertNotNull(collection);
            final Document first = collection.find().first();
            Assert.assertNotNull(first);
            Assert.assertEquals(first.toJson(), "Hello log", first.getString("message"));
        }
    }
}
