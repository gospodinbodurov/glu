/*
 * Copyright (c) 2011 Yan Pujante
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package org.linkedin.glu.orchestration.engine.delta

import grails.persistence.Entity

/**
 * @author yan@pongasoft.com */
@Entity
class UserCustomDeltaDefinition
{
  static constraints = {
    content(nullable: false, blank: false)
    // name of this entry must be unique for a given user only...
    name(nullable: false, blank: false, unique: 'username', validator: { val, obj ->
      println "name validator: [${val}] / [${obj.customDeltaDefinition.name}] "
      return val == obj.customDeltaDefinition.name
    })
    description(nullable: true, blank: true, validator: { val, obj ->
      println "description validator: [${val}] / [${obj.customDeltaDefinition.description}] "
      return val == obj.customDeltaDefinition.description
    })
    username(nullable: true, blank: false)
    contentVersion(nullable: false)
    shareable(nullable: false, validator: { val, obj ->
      println "shareable validator: [${val}] / [${obj.username}] "
      // does not make sense to have shareable set to false when the username is undefined!
      if(!val)
        return obj.username != null
      else
        return true
    })
  }

  static mapping = {
    columns {
      content type: 'text'
    }
  }

  /**
   * Automatically populated by gorm
   */
  Date dateCreated

  /**
   * Automatically populated by gorm
   */
  Date lastUpdated

  /**
   * Represent name from the definition (denormalized)
   */
  String name

  /**
   * Represent description from the definition (denormalized)
   */
  String description

  /**
   * Which user this object belongs to. YP note: not using a foreign key contraint on purpose: the
   * username may be <code>null</code> for 'any' user (custom deltas that belong to any user).
   */
  String username

  /**
   * If a user creates a custom delta, by default other users can see it too
   */
  boolean shareable = true

  /**
   * _content is a serialized version of the custom delta definition
   */
  private String _content
  private CustomDeltaDefinition _customDeltaDefinition

  /**
   * The version to be able to properly deserialize if there is a need
   */
  int contentVersion

  /**
   * Will be injected automatically */
  CustomDeltaDefinitionSerializer customDeltaDefinitionSerializer

  String getContent()
  {
    return _content
  }

  void setContent(String content)
  {
    _content = content
  }

  CustomDeltaDefinition getCustomDeltaDefinition()
  {
    return _customDeltaDefinition
  }

  void setCustomDeltaDefinition(CustomDeltaDefinition customDeltaDefinition)
  {
    _customDeltaDefinition = customDeltaDefinition
    name = customDeltaDefinition.name
    description = customDeltaDefinition.description
  }

  void beforeInsert()
  {
    println "before insert ${username}/${name}"
    _content = customDeltaDefinitionSerializer.serialize(customDeltaDefinition)
  }

  void beforeUpdate()
  {
    println "before update ${username}/${name}"
    _content = customDeltaDefinitionSerializer.serialize(customDeltaDefinition)
  }

  void afterLoad()
  {
    println "after load ${username}/${name}"
    customDeltaDefinition = serializer.deserialize(content, contentVersion)
  }

  static transients = ['customDeltaDefinition', '_customDeltaDefinition', '_content', 'customDeltaDefinitionSerializer']
}
