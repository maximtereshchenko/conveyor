package com.github.maximtereshchenko.conveyor.domain;

sealed interface ManualTemplateModel extends TemplateModel permits NoTemplateModel, OtherManualTemplateModel {}
