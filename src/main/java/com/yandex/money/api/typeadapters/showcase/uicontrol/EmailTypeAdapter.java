package com.yandex.money.api.typeadapters.showcase.uicontrol;

import com.yandex.money.api.model.showcase.components.uicontrol.Email;

/**
 * Type adapter for {@link Email} component.
 *
 * @author Anton Ermak (ermak@yamoney.ru)
 */
public final class EmailTypeAdapter extends ParameterControlTypeAdapter<Email, Email.Builder> {

    public static final EmailTypeAdapter INSTANCE = new EmailTypeAdapter();

    private EmailTypeAdapter() {
    }

    @Override
    protected Email.Builder createBuilderInstance() {
        return new Email.Builder();
    }

    @Override
    protected Email createInstance(Email.Builder builder) {
        return builder.create();
    }

    @Override
    protected Class<Email> getType() {
        return Email.class;
    }
}
