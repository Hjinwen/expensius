/*
 * Copyright (C) 2016 Mantas Varnagiris.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 */

package com.mvcoding.expensius.feature.report

import com.mvcoding.expensius.feature.Filter
import com.mvcoding.expensius.model.aFixedTimestampProvider
import com.mvcoding.expensius.model.anAppUser
import com.mvcoding.expensius.service.AppUserService
import com.nhaarman.mockito_kotlin.mock
import org.junit.Test

class FilterPresenterTest {
    val appUser = anAppUser()
    val appUserService: AppUserService = mock()
    val filter = Filter(appUserService, aFixedTimestampProvider())
    val view: FilterPresenter.View = mock()
    val presenter = FilterPresenter()

    @Test
    fun `shows initial values`() {
        presenter.attach(view)

        //        verify(view).showInterval(appUser.settings.reportPeriod, filter.)
    }
}