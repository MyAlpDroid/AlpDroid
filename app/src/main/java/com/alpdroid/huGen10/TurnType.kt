package com.alpdroid.huGen10


//Taking Class TurnType from OSMAnd

class TurnType {

    val activeCommonLaneTurn: Int
        get() {
            if (lanes == null || lanes!!.size == 0) {
                return C
            }
            for (i in lanes!!.indices) {
                if (lanes!![i] % 2 == 1) {
                    return getPrimaryTurn(lanes!![i])
                }
            }
            return C
        }

    fun toXmlString(): String {
        when (value) {
            C -> return "C"
            TL -> return "TL"
            TSLL -> return "TSLL"
            TSHL -> return "TSHL"
            TR -> return "TR"
            TSLR -> return "TSLR"
            TSHR -> return "TSHR"
            KL -> return "KL"
            KR -> return "KR"
            TU -> return "TU"
            TRU -> return "TRU"
            OFFR -> return "OFFR"
            RNDB -> return "RNDB$exitOut"
            RNLB -> return "RNLB$exitOut"
        }
        return "C"
    }

    val value: Int
    private var exitOut = 0

    // calculated Clockwise head rotation if previous direction to NORTH
    // calculated clockwise head rotation if previous direction to NORTH
    var turnAngle = 0f
    var isSkipToSpeak = false

    // lanes encoded as array of int
    // 0 bit - 0/1 - to use or not
    // 1-5 bits - additional turn info
    // 6-10 bits - secondary turn
    // 11-15 bits - tertiary turn
    var lanes: IntArray? = null
    var isPossibleLeftTurn = false
    var isPossibleRightTurn = false

    constructor(
        value: Int, exitOut: Int, turnAngle: Float, skipToSpeak: Boolean, lanes: IntArray?,
        possiblyLeftTurn: Boolean, possiblyRightTurn: Boolean
    ) {
        this.value = value
        this.exitOut = exitOut
        this.turnAngle = turnAngle
        isSkipToSpeak = skipToSpeak
        this.lanes = lanes
        isPossibleLeftTurn = possiblyLeftTurn
        isPossibleRightTurn = possiblyRightTurn
    }

    private constructor(vl: Int) {
        value = vl
    }

    val isLeftSide: Boolean
        get() = value == RNLB || value == TRU

    fun setExitOut(exitOut: Int) {
        this.exitOut = exitOut
    }

    fun getExitOut(): Int {
        return exitOut
    }

    //$NON-NLS-1$
    val isRoundAbout: Boolean
        get() = value == RNDB || value == RNLB //$NON-NLS-1$

    fun keepLeft(): Boolean {
        return value == KL
    }

    fun keepRight(): Boolean {
        return value == KR
    }

    fun goAhead(): Boolean {
        return value == C
    }

    override fun toString(): String {
        var vl: String? = null
        if (isRoundAbout) {
            vl = "Take " + getExitOut() + " exit"
        } else if (value == C) {
            vl = "Go ahead"
        } else if (value == TSLL) {
            vl = "Turn slightly left"
        } else if (value == TL) {
            vl = "Turn left"
        } else if (value == TSHL) {
            vl = "Turn sharply left"
        } else if (value == TSLR) {
            vl = "Turn slightly right"
        } else if (value == TR) {
            vl = "Turn right"
        } else if (value == TSHR) {
            vl = "Turn sharply right"
        } else if (value == TU) {
            vl = "Make uturn"
        } else if (value == TRU) {
            vl = "Make uturn"
        } else if (value == KL) {
            vl = "Keep left"
        } else if (value == KR) {
            vl = "Keep right"
        } else if (value == OFFR) {
            vl = "Off route"
        }
        if (vl != null) {
            if (lanes != null) {
                vl += " (" + lanesToString(lanes!!) + ")"
            }
            return vl
        }
        return super.toString()
    }

    companion object {
        const val C = 1 //"C"; // continue (go straight) //$NON-NLS-1$
        const val TL = 2 // turn left //$NON-NLS-1$
        const val TSLL = 3 // turn slightly left //$NON-NLS-1$
        const val TSHL = 4 // turn sharply left //$NON-NLS-1$
        const val TR = 5 // turn right //$NON-NLS-1$
        const val TSLR = 6 // turn slightly right //$NON-NLS-1$
        const val TSHR = 7 // turn sharply right //$NON-NLS-1$
        const val KL = 8 // keep left //$NON-NLS-1$
        const val KR = 9 // keep right//$NON-NLS-1$
        const val TU = 10 // U-turn //$NON-NLS-1$
        const val TRU = 11 // Right U-turn //$NON-NLS-1$
        const val OFFR = 12 // Off route //$NON-NLS-1$
        const val RNDB = 13 // Roundabout
        const val RNLB = 14 // Roundabout left
        fun straight(): TurnType {
            return valueOf(C, false)
        }

        fun fromString(s: String?, leftSide: Boolean): TurnType? {
            var t: TurnType? = null
            if ("C" == s) {
                t = valueOf(C, leftSide)
            } else if ("TL" == s) {
                t = valueOf(TL, leftSide)
            } else if ("TSLL" == s) {
                t = valueOf(TSLL, leftSide)
            } else if ("TSHL" == s) {
                t = valueOf(TSHL, leftSide)
            } else if ("TR" == s) {
                t = valueOf(TR, leftSide)
            } else if ("TSLR" == s) {
                t = valueOf(TSLR, leftSide)
            } else if ("TSHR" == s) {
                t = valueOf(TSHR, leftSide)
            } else if ("KL" == s) {
                t = valueOf(KL, leftSide)
            } else if ("KR" == s) {
                t = valueOf(KR, leftSide)
            } else if ("TU" == s) {
                t = valueOf(TU, leftSide)
            } else if ("TRU" == s) {
                t = valueOf(TRU, leftSide)
            } else if ("OFFR" == s) {
                t = valueOf(OFFR, leftSide)
            } else if (s != null && (s.startsWith("EXIT") ||
                        s.startsWith("RNDB") || s.startsWith("RNLB"))
            ) {
                try {
                    t = getExitTurn(s.substring(4).toInt(), 0f, leftSide)
                } catch (e: NumberFormatException) {
                    e.printStackTrace()
                }
            }
            if (t == null) {
                t = straight()
            }
            return t
        }

        fun valueOf(vs: Int, leftSide: Boolean): TurnType {
            var vs2 = vs
            if (vs == TU && leftSide) {
                vs2 = TRU
            } else if (vs == RNDB && leftSide) {
                vs2 = RNLB
            }
            return TurnType(vs2)

        }

        fun getExitTurn(out: Int, angle: Float, leftSide: Boolean): TurnType {
            val r = valueOf(RNDB, leftSide) //$NON-NLS-1$
            r.exitOut = out
            r.turnAngle = angle
            return r
        }

        // Note that the primary turn will be the one displayed on the map.
        fun setPrimaryTurnAndReset(lanes: IntArray, lane: Int, turnType: Int) {
            lanes[lane] = turnType shl 1
        }

        fun getPrimaryTurn(laneValue: Int): Int {
            // Get the primary turn modifier for the lane
            return laneValue shr 1 and (1 shl 4) - 1
        }

        fun setSecondaryTurn(lanes: IntArray, lane: Int, turnType: Int) {
            lanes[lane] = lanes[lane] and (15 shl 5).inv()
            lanes[lane] = lanes[lane] or (turnType shl 5)
        }

        fun setPrimaryTurn(lanes: IntArray, lane: Int, turnType: Int) {
            lanes[lane] = lanes[lane] and (15 shl 1).inv()
            lanes[lane] = lanes[lane] or (turnType shl 1)
        }

        fun getSecondaryTurn(laneValue: Int): Int {
            // Get the secondary turn modifier for the lane
            return laneValue shr 5 and (1 shl 5) - 1
        }

        fun setPrimaryTurnShiftOthers(lanes: IntArray, lane: Int, turnType: Int) {
            val pt = getPrimaryTurn(lanes[lane])
            val st = getSecondaryTurn(lanes[lane])
            //int tt = getTertiaryTurn(lanes[lane]); is lost here
            setPrimaryTurnAndReset(lanes, lane, turnType)
            setSecondaryTurn(lanes, lane, pt)
            setTertiaryTurn(lanes, lane, st)
        }

        fun setSecondaryToPrimary(lanes: IntArray, lane: Int) {
            val st = getSecondaryTurn(lanes[lane])
            val pt = getPrimaryTurn(lanes[lane])
            setPrimaryTurn(lanes, lane, st)
            setSecondaryTurn(lanes, lane, pt)
        }

        fun setTertiaryToPrimary(lanes: IntArray, lane: Int) {
            val st = getSecondaryTurn(lanes[lane])
            val pt = getPrimaryTurn(lanes[lane])
            val tt = getTertiaryTurn(lanes[lane])
            setPrimaryTurn(lanes, lane, tt)
            setSecondaryTurn(lanes, lane, pt)
            setTertiaryTurn(lanes, lane, st)
        }

        fun setTertiaryTurn(lanes: IntArray, lane: Int, turnType: Int) {
            lanes[lane] = lanes[lane] and (15 shl 10).inv()
            lanes[lane] = lanes[lane] or (turnType shl 10)
        }

        fun getTertiaryTurn(laneValue: Int): Int {
            // Get the tertiary turn modifier for the lane
            return laneValue shr 10
        }

        fun lanesToString(lns: IntArray): String {
            val s = StringBuilder()
            for (h in lns.indices) {
                if (h > 0) {
                    s.append("|")
                }
                if (lns[h] % 2 == 1) {
                    s.append("+")
                }
                var pt = getPrimaryTurn(lns[h])
                if (pt == 0) {
                    pt = 1
                }
                s.append(valueOf(pt, false).toXmlString())
                val st = getSecondaryTurn(lns[h])
                if (st != 0) {
                    s.append(",").append(valueOf(st, false).toXmlString())
                }
                val tt = getTertiaryTurn(lns[h])
                if (tt != 0) {
                    s.append(",").append(valueOf(tt, false).toXmlString())
                }
            }
            return s.toString()
        }

        fun lanesFromString(lanesString: String): IntArray? {
            if ((lanesString.isEmpty())) {
                return null
            }
            val lanesArr = lanesString.split("\\|").toTypedArray()
            val lanes = IntArray(lanesArr.size)
            for (l in lanesArr.indices) {
                val lane = lanesArr[l]
                val turns = lane.split(",").toTypedArray()
                var primaryTurn: TurnType? = null
                var secondaryTurn: TurnType? = null
                var tertiaryTurn: TurnType? = null
                var plus = false
                for (i in turns.indices) {
                    var turn = turns[i]
                    if (i == 0) {
                        plus = turn.length > 0 && turn[0] == '+'
                        if (plus) {
                            turn = turn.substring(1)
                        }
                        primaryTurn = fromString(turn, false)
                    } else if (i == 1) {
                        secondaryTurn = fromString(turn, false)
                    } else if (i == 2) {
                        tertiaryTurn = fromString(turn, false)
                    }
                }
                setPrimaryTurnAndReset(lanes, l, primaryTurn!!.value)
                if (secondaryTurn != null) {
                    setSecondaryTurn(lanes, l, secondaryTurn.value)
                }
                if (tertiaryTurn != null) {
                    setTertiaryTurn(lanes, l, tertiaryTurn.value)
                }
                if (plus) {
                    lanes[l] = lanes[l] or 1
                }
            }
            return lanes
        }

        fun isLeftTurn(type: Int): Boolean {
            return type == TL || type == TSHL || type == TSLL || type == TU || type == KL
        }

        fun isLeftTurnNoUTurn(type: Int): Boolean {
            return type == TL || type == TSHL || type == TSLL || type == KL
        }

        fun isRightTurn(type: Int): Boolean {
            return type == TR || type == TSHR || type == TSLR || type == TRU || type == KR
        }

        fun isRightTurnNoUTurn(type: Int): Boolean {
            return type == TR || type == TSHR || type == TSLR || type == KR
        }

        fun isSlightTurn(type: Int): Boolean {
            return type == TSLL || type == TSLR || type == C || type == KL || type == KR
        }

        fun isKeepDirectionTurn(type: Int): Boolean {
            return type == C || type == KL || type == KR
        }

        fun hasAnySlightTurnLane(type: Int): Boolean {
            return (isSlightTurn(getPrimaryTurn(type))
                    || isSlightTurn(getSecondaryTurn(type))
                    || isSlightTurn(getTertiaryTurn(type)))
        }

        fun hasAnyTurnLane(type: Int, turn: Int): Boolean {
            return getPrimaryTurn(type) == turn || getSecondaryTurn(type) == turn || getTertiaryTurn(
                type
            ) == turn
        }

        fun collectTurnTypes(lane: Int, set: HashSet<Int>) {
            var pt = getPrimaryTurn(lane)
            if (pt != 0) {
                set.add(pt)
            }
            pt = getSecondaryTurn(lane)
            if (pt != 0) {
                set.add(pt)
            }
            pt = getTertiaryTurn(lane)
            if (pt != 0) {
                set.add(pt)
            }
        }

        fun orderFromLeftToRight(type: Int): Int {
            return when (type) {
                TU -> -5
                TSHL -> -4
                TL -> -3
                TSLL -> -2
                KL -> -1
                TRU -> 5
                TSHR -> 4
                TR -> 3
                TSLR -> 2
                KR -> 1
                else -> 0
            }
        }

        fun convertType(lane: String): Int {
            val turn: Int
            // merge should be recognized as continue route (but it could displayed differently)
            turn = if (lane == "merge_to_left") {
                C
            } else if (lane == "merge_to_right") {
                C
            } else if (lane == "none" || lane == "through") {
                C
            } else if (lane == "slight_right") {
                TSLR
            } else if (lane == "slight_left") {
                TSLL
            } else if (lane == "right") {
                TR
            } else if (lane == "left") {
                TL
            } else if (lane == "sharp_right") {
                TSHR
            } else if (lane == "sharp_left") {
                TSHL
            } else if (lane == "reverse") {
                TU
            } else {
                // Unknown string
                C
                //			continue;
            }
            return turn
        }
    }
}