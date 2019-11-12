package com.github.lppedd.cc.configuration;

import javax.swing.*;

import org.jetbrains.annotations.NotNull;

import com.github.lppedd.cc.ConventionalCommitBundle;
import com.github.lppedd.cc.configuration.ConventionalCommitConfig.CompletionType;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBRadioButton;

/**
 * @author Edoardo Luppi
 */
public class ConventionalCommitConfigurableGui {
  private JPanel rootPanel;
  private JBRadioButton isAutoPopup;
  private JBRadioButton isTemplate;
  private JBLabel info;
  private final ButtonGroup group = new ButtonGroup();

  public ConventionalCommitConfigurableGui(final ConventionalCommitBundle bundle) {
    this();
    finishUpComponents(bundle);
  }

  private ConventionalCommitConfigurableGui() {}

  @NotNull
  public JPanel getRootPanel() {
    return rootPanel;
  }

  public CompletionType getCompletionType() {
    if (isAutoPopup.isSelected()) {
      return CompletionType.AUTOPOPUP;
    }

    if (isTemplate.isSelected()) {
      return CompletionType.TEMPLATE;
    }

    throw new IllegalStateException("A radio button should be selected");
  }

  public void setCompletionType(final CompletionType completionType) {
    switch (completionType) {
      case AUTOPOPUP:
        isAutoPopup.setSelected(true);
        break;
      case TEMPLATE:
        isTemplate.setSelected(true);
        break;
      default:
        break;
    }
  }

  private void finishUpComponents(final ConventionalCommitBundle bundle) {
    group.add(isAutoPopup);
    group.add(isTemplate);

    info.setText(bundle.get("conventionalCommit.setting.info"));
    isAutoPopup.setText(bundle.get("conventionalCommit.setting.autoPopup"));
    isTemplate.setText(bundle.get("conventionalCommit.setting.template"));
  }
}
