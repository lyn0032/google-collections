/*
 * Copyright (C) 2008 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.common.collect.testing.features;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Use this to meta-annotate XxxFeature.Require annotations, so that those
 * annotations can be used to decide whether to apply a test to a given
 * class-under-test.
 * <br>
 * This is needed because annotations can't implement interfaces, which is also
 * why reflection is used to extract values from the properties of the various
 * annotations.
 *
 * @see CollectionFeature.Require
 *
 * @author George van den Driessche
 */
@Target(value = {java.lang.annotation.ElementType.ANNOTATION_TYPE})
@Retention(value = RetentionPolicy.RUNTIME)
@Documented
public @interface TesterAnnotation {
}
