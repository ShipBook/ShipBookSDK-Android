package io.shipbook.shipbooksdk.Util

import android.view.View
import android.view.ViewGroup

/*
 *
 * Created by Elisha Sterngold on 28/02/2018.
 * Copyright © 2018 ShipBook Ltd. All rights reserved.
 *
 */

internal val ViewGroup.views: List<View>
    get() = (0 until childCount).map { getChildAt(it) }
