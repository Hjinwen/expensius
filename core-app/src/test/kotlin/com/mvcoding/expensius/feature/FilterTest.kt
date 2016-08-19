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

package com.mvcoding.expensius.feature

import com.mvcoding.expensius.extensions.interval
import com.mvcoding.expensius.model.TransactionState.PENDING
import com.mvcoding.expensius.model.TransactionType.EXPENSE
import com.mvcoding.expensius.model.aFixedTimestampProvider
import com.mvcoding.expensius.model.anAppUser
import com.mvcoding.expensius.service.AppUserService
import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.whenever
import org.joda.time.Interval
import org.junit.Test
import rx.Observable.just
import rx.observers.TestSubscriber

class FilterTest {
    val subscriber: TestSubscriber<FilterData> = TestSubscriber.create<FilterData>()
    val appUser = anAppUser()
    val appUserService: AppUserService = mock<AppUserService>().apply { whenever(this.appUser()).thenReturn(just(appUser)) }
    val timestampProvider = aFixedTimestampProvider()
    val defaultFilterData = FilterData(anAppUser().settings.reportPeriod.interval(timestampProvider.currentTimestamp()))
    val filter = Filter(appUserService, timestampProvider)

    @Test
    fun `initially gives empty filter data`() {
        filter.filterData().subscribe(subscriber)

        val filterData = filter.getCurrentFilterData()

        subscriber.assertValue(defaultFilterData)
        assertThat(filterData, equalTo(defaultFilterData))
    }

    @Test
    fun `changing filter values emits updated filter`() {
        filter.filterData().subscribe(subscriber)

        filter.setTransactionType(EXPENSE)
        filter.setTransactionState(PENDING)
        filter.setInterval(Interval(0, 1))

        val filterData = filter.getCurrentFilterData()

        subscriber.assertValues(
                defaultFilterData,
                defaultFilterData.copy(transactionType = EXPENSE),
                defaultFilterData.copy(transactionType = EXPENSE, transactionState = PENDING),
                defaultFilterData.copy(Interval(0, 1), EXPENSE, PENDING))
        assertThat(filterData, equalTo(FilterData(Interval(0, 1), EXPENSE, PENDING)))
    }

    @Test
    fun `clearing filter values emits updated filter`() {
        filter.setInterval(Interval(0, 1))
        filter.setTransactionType(EXPENSE)
        filter.setTransactionState(PENDING)
        filter.filterData().subscribe(subscriber)

        filter.clearTransactionType()
        filter.clearTransactionState()

        val filterData = filter.getCurrentFilterData()

        subscriber.assertValues(
                FilterData(Interval(0, 1), EXPENSE, PENDING),
                FilterData(Interval(0, 1), transactionState = PENDING),
                FilterData(Interval(0, 1)))
        assertThat(filterData, equalTo(FilterData(Interval(0, 1))))
    }
}