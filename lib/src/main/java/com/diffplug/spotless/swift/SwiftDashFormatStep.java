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

import java.io.File;
import java.io.IOException;
import java.io.Serial;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.Nullable;

import com.diffplug.spotless.ForeignExe;
import com.diffplug.spotless.FormatterFunc;
import com.diffplug.spotless.FormatterStep;
import com.diffplug.spotless.ProcessRunner;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

public final class SwiftDashFormatStep implements Serializable {
	@Serial
	private static final long serialVersionUID = 1L;
	private static final String DEFAULT_VERSION = "6.2.0";
	private static final String NAME = "swift-format";

	private final String version;
	private final @Nullable String pathToExe;

	private SwiftDashFormatStep(String version, @Nullable String pathToExe) {
		this.version = Objects.requireNonNull(version, "version");
		this.pathToExe = pathToExe;
	}

	public static SwiftDashFormatStep withVersion(String version) {
		return new SwiftDashFormatStep(version, null);
	}

	public SwiftDashFormatStep withPathToExe(String pathToExe) {
		return new SwiftDashFormatStep(version, pathToExe);
	}

	public FormatterStep create() {
		return FormatterStep.createLazy(NAME, this::createRoundtrip, RoundtripState::toEquality, EqualityState::toFunc);
	}

	private RoundtripState createRoundtrip() {
		// TODO Update advice.
		String trackingIssue = "\n  github issue to handle this better: https://github.com/diffplug/spotless/issues/674";
		ForeignExe exeAbsPath = ForeignExe.nameAndVersion(NAME, version)
				.pathToExe(pathToExe)
				.versionRegex(Pattern.compile("^(.*)\n$"))
				.fixCantFind("Try running {@code pip install black=={version}}, or else tell Spotless where it is with {@code black().pathToExe('path/to/executable')}" + trackingIssue)
				.fixWrongVersion("Try running {@code pip install --force-reinstall black=={version}}, or else specify {@code black('{versionFound}')} to Spotless" + trackingIssue);
		return new RoundtripState(version, exeAbsPath);
	}

	public static String defaultVersion() {
		return DEFAULT_VERSION;
	}

	static class RoundtripState implements Serializable {
		@Serial
		private static final long serialVersionUID = 1L;

		final String version;
		final ForeignExe exe;

		RoundtripState(String version, ForeignExe exe) {
			this.version = version;
			this.exe = exe;
		}

		private EqualityState toEquality() {
			return new EqualityState(version, exe);
		}
	}

	@SuppressFBWarnings("SE_TRANSIENT_FIELD_NOT_RESTORED")
	static class EqualityState implements Serializable {
		@Serial
		private static final long serialVersionUID = 1L;

		final String version;
		final transient ForeignExe exe;
		private transient @Nullable List<String> args;

		EqualityState(String version, ForeignExe exeAbsPath) {
			this.version = version;
			this.exe = Objects.requireNonNull(exeAbsPath);
		}

		String format(ProcessRunner runner, String input, File file) throws IOException, InterruptedException {
			if (args == null) {
				args = List.of(exe.confirmVersionAndGetAbsolutePath(), "--parallel", "--strict");
			}
			final List<String> finalArgs = Stream.concat(args.stream(), Stream.of(file.getAbsolutePath()))
					.collect(Collectors.toList());
			return runner.exec(input.getBytes(StandardCharsets.UTF_8), finalArgs).assertExitZero(StandardCharsets.UTF_8);
		}

		FormatterFunc.Closeable toFunc() {
			ProcessRunner runner = new ProcessRunner();
			return FormatterFunc.Closeable.of(runner, this::format);
		}
	}
}
