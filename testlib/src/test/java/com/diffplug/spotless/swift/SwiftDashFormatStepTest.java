/*
 * Copyright 2016-2026 DiffPlug
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.diffplug.spotless.swift;

import org.junit.jupiter.api.Test;

import com.diffplug.spotless.ResourceHarness;
import com.diffplug.spotless.StepHarnessWithFile;

//@SwiftDashFormatTest
class SwiftDashFormatStepTest extends ResourceHarness {
	@Test
	void behavior() {
		try (StepHarnessWithFile harness = StepHarnessWithFile.forStep(this, SwiftDashFormatStep.withVersion(SwiftDashFormatStep.defaultVersion()).create())) {
			setFile(".swift-format").toResource("swift/swift-format/.swift-format");
			harness.testResource("swift/swift-format/basic.dirty", "swift/swift-format/basic.clean");
		}
	}
}
